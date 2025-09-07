package com.app.boardcraftback.domain.entity.user;

import com.app.boardcraftback.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
@Getter @ToString @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(length = 36, columnDefinition = "CHAR(36)", name = "id", nullable = false)
    private String id;

    @Column(length = 255, name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 50, name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    )
    @Column(name = "role_name", length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<RoleType> roles = new HashSet<>();
}

