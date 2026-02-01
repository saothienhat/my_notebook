
package com.my.domain.entity;
import com.my.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "platform_user")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private long id;
    @Column(name = "person_id")
    private String personId; 
    @Column(name = "account")
    private String account;
    @Column(name = "email")
    private String email;
    @Column(name = "name")
    private String name;
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}



import com.my.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String name;
}



public enum UserStatus {
    ACTIVE,
    INACTIVE
}


public enum UserRole {
    ADMIN("ROLE_ADMIN")
}



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final RoleRepository roleRepository;
  private final DirectoryService DirectoryService;

  
  @Transactional(readOnly = true)
  public Page<UserView> search(String keyword, Pageable pageable) {
    String searchText = keyword == null ? "" : keyword.trim().toLowerCase();
    PageRequest pagingData = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    val pageRequest = pagingData.withSort(Sort.by("id").descending());
    Specification<User> keywordSpec = null;
    if (!searchText.isEmpty()) {
      keywordSpec =
          (root, query, cb) ->
              cb.or(
                  cb.like(root.get("account"), "%" + searchText + "%"),
                  cb.like(root.get("email"), "%" + searchText + "%"),
                  cb.like(root.get("name"), "%" + searchText + "%"));
    }
    Page<User> entities = repository.findAll(keywordSpec, pageRequest);
    List<UserView> list = entities.stream().map(UserView::fromEntity).toList();
    int from = (int) pageable.getOffset();
    int to = Math.min(from + pageable.getPageSize(), list.size());
    if (from >= list.size()) {
      return Page.empty(pageable);
    }
    List<UserView> pageContent = list.subList(from, to);
    return new PageImpl<>(pageContent, pageable, list.size());
  }


  @Transactional
  public UserView create(CreateUserRequest request) {
    log.info("Start Creating User with roles {} into Astro platform", request.getRoles());
    User existedUser = repository.findByAccount(request.getAccount());
    if (existedUser != null) {
      throw new IllegalArgumentException("User already existed in platform for creating");
    }
    PersonView personView = DirectoryService.getPerson(request.getAccount());
    if (personView == null) {
      log.error("Person not exists in  for creating Astro user: {}", request.getAccount());
      throw new IllegalArgumentException("Person not exists in  for creating Astro user");
    }
    List<Role> roleEntities = roleRepository.findByRoleIn(request.getRoles());
    User jpaUser = new User();
    jpaUser.setPersonId(personView.getPersonId());
    jpaUser.setAccount(personView.getAccount());
    jpaUser.setName(personView.getRomaName());
    jpaUser.setEmail(personView.getMailaddress());
    jpaUser.setStatus(UserStatus.ACTIVE);
    jpaUser.setRoles(new HashSet<>(roleEntities));
    jpaUser.setCreatedAt(Instant.now());
    jpaUser.setUpdatedAt(Instant.now());
    User newUser = repository.save(jpaUser);
    log.info("Finish creating User with roles {} into Astro platform", request.getRoles());
    return UserView.fromEntity(newUser);
  }

  @Transactional
  public UserView updateUser(long id, UpdateUserRequest request) {
    log.info("Start Updating User id {} with roles {} into Astro", id, request.getRoles());
    User jpaUser =
        repository
            .findById(id)
            .orElseThrow(
                () -> {
                  log.error("User not found with id {}", id);
                  return new IllegalArgumentException("User not found with id " + id);
                });
    List<Role> jpaRoles = roleRepository.findByRoleIn(request.getRoles());
    jpaUser.getRoles().clear();
    jpaUser.setRoles(new HashSet<>(jpaRoles));
    jpaUser.setUpdatedAt(Instant.now());
    User updatedUser = repository.save(jpaUser);
    log.info("Updated User {} with roles: {}", id, request.getRoles());
    return UserView.fromEntity(updatedUser);
  }
}


import com.company.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, PagingAndSortingRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    User findByAccount(String account);

}



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>  {

    Role findByRole(UserRole role);

    List<Role> findByRoleIn(List<UserRole> roles);


}





// ==================== Tables ====================

CREATE TABLE "role" (
id bigint NOT NULL,
"role" varchar(50) NOT NULL,
"name" varchar(100) NULL,
CONSTRAINT role_pk PRIMARY KEY (id),
CONSTRAINT role_unique UNIQUE (role)
);


CREATE TABLE user_role (
	user_id int8 NOT NULL,
	role_id int8 NOT NULL,
	CONSTRAINT user_role_unique UNIQUE (user_id, role_id)
);