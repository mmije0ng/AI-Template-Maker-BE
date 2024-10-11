package com.backend.sparkle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name="AddressListToAddress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressListToAddress extends BaseEntity{

    //아이디
    @Id //pk
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto increment
    private Long id;

    //사용자 아이디
    //fk
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    //주소아이디
    //fk
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;



}
