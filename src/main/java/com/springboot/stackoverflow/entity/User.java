package com.springboot.stackoverflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "photoName")
    private String photoName;
    @Column(name = "photoType")
    private String photoType;

    @Column(name = "photoLink")
    private String photoLink;

    @Column(name = "photoSize")
    private Long photoSize;

    @NotBlank(message = "Username Cannot be null")
    @Size(min = 3,max = 15 ,message = "Username must be between 3 - 15 characters")
    @Column(name = "username")
    private String userName;

    @NotBlank(message = "email cannot be empty")
    @Column(name = "email")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Column(name = "password")
    private String password;
    @Column(name = "reputation")
    private Integer reputation;
    @Column(columnDefinition = "TEXT", name = "about")
    private String about;
    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;
    @Column(name = "country")
    private String country = "NA";
    @Column(name = "title")
    private String title;
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "photo")
    private String photo = null;
    @ManyToMany()
    @JoinTable(
            name = "FollowRelation",
            joinColumns = @JoinColumn(name = "follower_id"), // field from current class
            inverseJoinColumns = @JoinColumn(name = "following_id") // field from other class
    )
    private List<User> followings;
    @ManyToMany()
    @JoinTable(
            name = "FollowRelation",
            joinColumns = @JoinColumn(name = "following_id"), // field from current class
            inverseJoinColumns = @JoinColumn(name = "follower_id") // field from other class
    )
    private List<User> followers;
    @ManyToMany()
    @JoinTable(
            name = "saved_question_user",
            joinColumns = @JoinColumn(name = "saved_user_id"), // field from current class
            inverseJoinColumns = @JoinColumn(name = "saved_question_id") // field from other class
    )
    private List<Question> savedQuestions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Comment> commentList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Answer> answerList;
    @ManyToMany
    @JoinTable(name = "user_badge",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    private List<Badge> userBadges;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Question> userQuestions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Vote> userVotes;
    @ManyToMany()
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> role;

    public User() {
    }

    public User(String userName, String email,
                String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.reputation = 0;
        this.about = "";
    }

    public List<Vote> getUserVote() {
        return userVotes;
    }

    public void setUserVote(List<Vote> userVote) {
        this.userVotes = userVote;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Role> getRole() {
        return role;
    }

    public void setRole(List<Role> role) {
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getReputation() {
        return reputation;
    }

    public void setReputation(Integer reputation) {
        this.reputation = reputation;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<User> getFollowings() {
        return followings;
    }

    public void setFollowings(List<User> followings) {
        this.followings = followings;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }

    public List<Badge> getUserBadges() {
        return userBadges;
    }

    public void setUserBadges(List<Badge> userBadges) {
        this.userBadges = userBadges;
    }

    public List<Question> getUserQuestions() {
        return userQuestions;
    }

    public void setUserQuestions(List<Question> userQuestions) {
        this.userQuestions = userQuestions;
    }


    public List<Question> getSavedQuestions() {
        return savedQuestions;
    }

    public void setSavedQuestions(List<Question> savedQuestions) {
        this.savedQuestions = savedQuestions;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Vote> getUserVotes() {
        return userVotes;
    }

    public void setUserVotes(List<Vote> userVotes) {
        this.userVotes = userVotes;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getPhotoType() {
        return photoType;
    }

    public void setPhotoType(String photoType) {
        this.photoType = photoType;
    }

    public String getPhotoLink() {
        return photoLink;
    }

    public void setPhotoLink(String photoLink) {
        this.photoLink = photoLink;
    }

    public Long getPhotoSize() {
        return photoSize;
    }

    public void setPhotoSize(Long photoSize) {
        this.photoSize = photoSize;
    }

    public void addFollower(User theUser) {
        if (followers == null) {
            followers = new ArrayList<>();
        }

        followers.add(theUser);
    }

    public void addFollowing(User theUser) {
        if (followings == null) {
            followings = new ArrayList<>();
        }

        followings.add(theUser);
    }

    public void addComment(Comment theComment) {
        if (commentList == null) {
            commentList = new ArrayList<>();
        }

        commentList.add(theComment);
        theComment.setUser(this);
    }

    public void addAnswer(Answer theAnswer) {
        if (answerList == null) {
            answerList = new ArrayList<>();
        }

        answerList.add(theAnswer);
    }

    public void addBadges(Badge theBadge) {
        if (userBadges == null) {
            userBadges = new ArrayList<>();
        }

        userBadges.add(theBadge);
    }

    public void addQuestion(Question theQuestion) {
        if (userQuestions == null) {
            userQuestions = new ArrayList<>();
        }

        userQuestions.add(theQuestion);
    }

    public void addRole(Role theRole) {
        if (role == null) {
            role = new ArrayList<>();
        }

        role.add(theRole);
    }

    public void addVote(Vote theVote) {
        if (userVotes == null) {
            userVotes = new ArrayList<>();
        }

        userVotes.add(theVote);
        theVote.setUser(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", reputation=" + reputation +
                ", about='" + about + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
