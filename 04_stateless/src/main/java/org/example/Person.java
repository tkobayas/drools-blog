package org.example;

import java.time.LocalDate;

public class Person {

    private String name;

    private LocalDate memberCreatedAt; // メンバー登録日

    public Person() {
    }

    public Person(String name, LocalDate memberCreatedAt) {
        this.name = name;
        this.memberCreatedAt = memberCreatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getMemberCreatedAt() {
        return memberCreatedAt;
    }

    public void setMemberCreatedAt(LocalDate memberCreatedAt) {
        this.memberCreatedAt = memberCreatedAt;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", memberCreatedAt=" + memberCreatedAt + "]";
    }

}
