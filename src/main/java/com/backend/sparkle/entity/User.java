package com.backend.sparkle.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="User")
public class User extends BaseEntity{
    //아이디
    @Id //pk
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto increment
    private Long id;

    //사용자의 이름
    @Column(name="user_name",nullable = false,length = 10)
    private String user_name;

    //로그인 아이디
    @Column(name="login_id",nullable = false,length = 20,unique = true)
    private String login_id;

    //로그인 비밀번호
    @Column(name="login_password",nullable = false,length = 20)
    private String login_password;

    //씨앗, 초기 값 0
    @Column(name="point",nullable = false,length = 20)
    private int point=0;





}
