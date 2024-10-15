package com.backend.sparkle.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="AddressList")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressList extends BaseEntity{
    //아이디
    @Id //pk
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto increment
    private Long id;

    //주소록별칭
    @Column(name = "address_list_name", length = 20, nullable = false)
    private String addressListName;



}
