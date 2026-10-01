package com.thanh.profileservice.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.thanh.profileservice.entity.UserProfile;

@Repository
public interface UserProfileRepository extends Neo4jRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<UserProfile> findAllByUserIdIn(Collection<String> userIds);

    List<UserProfile> findAllByUsernameLike(String username);

    // Follow relationships are managed with Cypher instead of an entity field, so saving a
    // profile never rewrites or drops its FOLLOWS edges.
    // Write queries need @Transactional: repository query methods default to read-only transactions.

    @Transactional
    @Query("MATCH (a:user_profile {userId: $followerId}), (b:user_profile {userId: $followeeId}) "
            + "MERGE (a)-[r:FOLLOWS]->(b) ON CREATE SET r.createdDate = datetime() "
            + "RETURN count(r)")
    Long follow(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    @Transactional
    @Query("MATCH (:user_profile {userId: $followerId})-[r:FOLLOWS]->(:user_profile {userId: $followeeId}) "
            + "DELETE r RETURN count(*)")
    Long unfollow(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    @Query("MATCH (:user_profile {userId: $followerId})-[r:FOLLOWS]->(:user_profile {userId: $followeeId}) "
            + "RETURN count(r)")
    Long countFollowRelation(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    @Query("MATCH (:user_profile)-[r:FOLLOWS]->(:user_profile {userId: $userId}) RETURN count(r)")
    Long countFollowers(@Param("userId") String userId);

    @Query("MATCH (:user_profile {userId: $userId})-[r:FOLLOWS]->(:user_profile) RETURN count(r)")
    Long countFollowing(@Param("userId") String userId);

    @Query("MATCH (f:user_profile)-[r:FOLLOWS]->(:user_profile {userId: $userId}) "
            + "RETURN f ORDER BY r.createdDate DESC")
    List<UserProfile> findFollowers(@Param("userId") String userId);

    @Query("MATCH (:user_profile {userId: $userId})-[r:FOLLOWS]->(f:user_profile) "
            + "RETURN f ORDER BY r.createdDate DESC")
    List<UserProfile> findFollowing(@Param("userId") String userId);

    // Friends = mutual follows
    @Query("MATCH (u:user_profile {userId: $userId})-[:FOLLOWS]->(f:user_profile)-[:FOLLOWS]->(u) "
            + "RETURN f ORDER BY f.username")
    List<UserProfile> findFriends(@Param("userId") String userId);
}
