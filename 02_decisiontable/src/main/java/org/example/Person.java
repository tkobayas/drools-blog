package org.example;

import java.time.LocalDate;

public class Person {

    private String name;

    private LocalDate memberCreatedAt; // メンバー登録日

    private MembershipCard card;

    public Person() {
    }

    public Person(String name, LocalDate memberCreatedAt, MembershipCard card) {
        this.name = name;
        this.memberCreatedAt = memberCreatedAt;
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MembershipCard getCard() {
        return card;
    }

    public void setCard(MembershipCard card) {
        this.card = card;
    }

    public LocalDate getMemberCreatedAt() {
        return memberCreatedAt;
    }

    public void setMemberCreatedAt(LocalDate memberCreatedAt) {
        this.memberCreatedAt = memberCreatedAt;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", memberCreatedAt=" + memberCreatedAt + ", card=" + card + "]";
    }

}
