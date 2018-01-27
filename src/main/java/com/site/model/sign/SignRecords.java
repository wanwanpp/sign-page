package com.site.model.sign;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;


/**
 * Created by wang0 on 2016/9/13.
 */

@Entity
@Table(name = "sign_records")
@Getter
@Setter
public class SignRecords {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;
    private String name;
    @Column(name = "come_time")
    private Timestamp comeTime;
    @Column(name = "leave_time")
    private Timestamp leaveTime;
    @Column(name = "total_mill")
    @JsonIgnore
    private Long totalMill;
    @Column(name = "str_time")
    private String strTime;

    public SignRecords(){}

    public SignRecords(String name) {
        this.name = name;
        this.comeTime = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SignRecords that = (SignRecords) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (comeTime != null ? !comeTime.equals(that.comeTime) : that.comeTime != null) return false;
        if (leaveTime != null ? !leaveTime.equals(that.leaveTime) : that.leaveTime != null) return false;
        if (totalMill != null ? !totalMill.equals(that.totalMill) : that.totalMill != null) return false;
        return strTime != null ? strTime.equals(that.strTime) : that.strTime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (comeTime != null ? comeTime.hashCode() : 0);
        result = 31 * result + (leaveTime != null ? leaveTime.hashCode() : 0);
        result = 31 * result + (totalMill != null ? totalMill.hashCode() : 0);
        result = 31 * result + (strTime != null ? strTime.hashCode() : 0);
        return result;
    }
}
