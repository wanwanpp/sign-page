package com.site.repository;


import com.site.model.sign.SignRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * Created by wang0 on 2016/9/13.
 */
public interface SignRecordsRepo extends JpaRepository<SignRecords, Long> {

    @Query("select s.comeTime from SignRecords s where s.id=?1")
    Timestamp selectComeTime(Long id);

    @Modifying
    @Transactional
    @Query("update SignRecords s set s.leaveTime=?1,s.totalMill=?2,s.strTime=?3 where s.id=?4")
    int setSendEnd(Timestamp leaveTime, Long total_mill, String str_time, Long id);

    @Query(value = "SELECT come_time FROM sign_records WHERE name=?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Timestamp selectDescZCometime(String name);

    @Query(value = "SELECT id FROM sign_records WHERE name=?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Long selectDescZId(String name);
}
