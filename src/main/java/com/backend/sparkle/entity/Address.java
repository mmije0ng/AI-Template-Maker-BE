package com.backend.sparkle.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity{
    //아이디
    @Id //pk
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto increment
    private Long id;

    //전화번호
    //Address테이블이 User테이블과 연결 ->발신번호
    //AddressListToAddress테이블과 연결 ->수신번호
    @Column(name="phone_number",nullable = false,length = 15)
    private String phoneNumber;

}
