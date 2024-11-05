package com.backend.sparkle.entity;

import com.backend.sparkle.dto.MessageDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@DynamicInsert
@DynamicUpdate
@Entity
@Table(name="Message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity{

    //아이디
    @Id //pk
    @GeneratedValue(strategy= GenerationType.IDENTITY) //auto increment
    private Long id;

    //사용자아이디
    //fk
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

//    //주소록_주소_매핑아이디
//    //fk
//    @ManyToOne
//    @JoinColumn(name = "address_list_address_id", nullable = false)
//    private AddressListToAddress addressListToAddress;

    //발신번호
    @Column(name = "send_phone_number", length = 15, nullable = false)
    private String sendPhoneNumber;

    //문자내용
    @Column(name = "message_content", length = 200, nullable = false)
    private String messageContent;

    //발송이미지url
    @Column(name = "message_image_url", length = 200, nullable = true)
    private String messageImageUrl;

    // MessageDto.SendRequestDto를 받아 Message 엔티티를 초기화하는 생성자
    public Message(User user, MessageDto.SendRequestDto requestDto) {
        this.user = user;
        this.sendPhoneNumber = requestDto.getSendPhoneNumber();
        this.messageContent = requestDto.getSendMessage();
        this.messageImageUrl = requestDto.getCompleteImageURL();
    }
}
