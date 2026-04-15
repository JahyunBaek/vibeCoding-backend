package com.example.commonsystem.notification.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.commonsystem.notification.domain.Notification;

@Mapper
public interface NotificationMapper {
    List<Notification> findByUser(@Param("userId") long userId, @Param("limit") int limit);
    int countUnread(@Param("userId") long userId);
    void insert(Notification notification);
    void markAsRead(@Param("notificationId") long notificationId, @Param("userId") long userId);
    void markAllAsRead(@Param("userId") long userId);
}
