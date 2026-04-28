package com.example.commonsystem.notification.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationPreferenceMapper {

    List<java.util.Map<String, Object>> findByUser(@Param("tenantId") Long tenantId,
                                                    @Param("userId") long userId);

    void upsert(@Param("tenantId") Long tenantId,
                @Param("userId") long userId,
                @Param("channel") String channel,
                @Param("enabled") boolean enabled,
                @Param("consented") boolean consented);

    boolean isChannelEnabled(@Param("tenantId") Long tenantId,
                             @Param("userId") long userId,
                             @Param("channel") String channel);
}
