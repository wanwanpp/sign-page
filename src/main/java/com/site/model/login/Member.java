package com.site.model.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Setter
@Getter
public class Member { //1

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Integer id;
    private Long stuId;

    @Column(name = "names")
    private String name;
    private Integer grade;
    private String loginName;
    private String pwd;
    @Column(name = "groups")

    private Integer group;
    private Integer isleader;
    private Integer isstart;
    private Long recordId;
    private Integer sex;
    private String phone;
    @ManyToMany(mappedBy = "members")
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    private List<Roles> roles = new ArrayList<Roles>();
}
