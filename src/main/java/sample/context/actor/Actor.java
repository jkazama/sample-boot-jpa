package sample.context.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import sample.context.Dto;
import sample.context.actor.type.ActorRoleType;

/**
 * Represents the user in the use case.
 * <p>
 * Also acts as UserDetails in authentication/authorization
 */
@Builder
public record Actor(
        /** User ID */
        String id,
        /** User Name */
        String name,
        /** Email Address */
        String mailAddress,
        /** Role of Users */
        ActorRoleType roleType,
        /** User Locale */
        Locale locale,
        /** User Access Channel */
        String channel,
        /** External information that identifies the user. (e.g. IP) */
        String source,
        /** Profile */
        String profile,
        /** Avatar URL */
        String avatar,
        /** Extended Attributes */
        Map<String, String> extensions,
        /** Group ID List */
        List<String> groupIds,
        /** Authority ID List */
        Set<String> authorityIds) implements Dto, UserDetails {

    /** Anonymous user constant */
    public static final Actor ANONYMOUS = Actor.of("unknown", ActorRoleType.ANONYMOUS);
    /** System user constant */
    public static final Actor SYSTEM = Actor.of("system", ActorRoleType.SYSTEM);

    /** {@inheritDoc} */
    @Override
    public String getUsername() {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public String getPassword() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorityIds.stream()
                .map(v -> new SimpleGrantedAuthority(v))
                .collect(Collectors.toSet());
    }

    public Actor addGroupId(String groupId) {
        this.groupIds.add(groupId);
        return this;
    }

    public Actor addGroupIds(Collection<String> groupIds) {
        this.groupIds.addAll(groupIds);
        return this;
    }

    public Actor addAuthorityId(String authority) {
        this.authorityIds.add(authority);
        return this;
    }

    public Actor addAuthorityIds(Set<String> authorities) {
        this.authorityIds.addAll(authorities);
        return this;
    }

    public ActorBuilder copyBuilder() {
        return Actor.builder()
                .id(id)
                .name(name)
                .mailAddress(mailAddress)
                .roleType(roleType)
                .locale(locale)
                .channel(channel)
                .source(source)
                .profile(profile)
                .avatar(avatar)
                .extensions(new HashMap<>(extensions))
                .groupIds(new ArrayList<>(groupIds))
                .authorityIds(new HashSet<>(authorityIds));
    }

    public static ActorBuilder builderDefault() {
        return Actor.builder()
                .locale(Locale.getDefault())
                .extensions(new HashMap<>())
                .groupIds(new ArrayList<>())
                .authorityIds(new HashSet<>());
    }

    public static Actor of(String id, ActorRoleType roleType) {
        return of(id, id, roleType);
    }

    public static Actor of(String id, String name, ActorRoleType roleType) {
        return Actor.builder()
                .id(id)
                .name(name)
                .roleType(roleType)
                .build();
    }

}
