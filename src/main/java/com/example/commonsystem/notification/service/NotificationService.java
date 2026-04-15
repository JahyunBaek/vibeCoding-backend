package com.example.commonsystem.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commonsystem.common.I18nService;
import com.example.commonsystem.notification.domain.Notification;
import com.example.commonsystem.notification.mapper.NotificationMapper;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final I18nService i18n;

    public NotificationService(NotificationMapper notificationMapper, I18nService i18n) {
        this.notificationMapper = notificationMapper;
        this.i18n = i18n;
    }

    public List<Notification> list(long userId) {
        return notificationMapper.findByUser(userId, 20);
    }

    public int unreadCount(long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Transactional
    public void create(Long tenantId, long userId, String type, String title, String message, String link) {
        Notification notification = new Notification(
            0, tenantId, userId, type, title, message, link, false, null
        );
        notificationMapper.insert(notification);
    }

    @Transactional
    public void markAsRead(long notificationId, long userId) {
        notificationMapper.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    /**
     * 게시글 작성자에게 댓글 알림을 생성한다.
     */
    @Transactional
    public void notifyPostComment(long postAuthorId, Long tenantId, String commenterName, long boardId, long postId, String locale) {
        String title = i18n.getMessage("notification.comment.title", locale, commenterName);
        String link = "/boards/" + boardId + "/posts/" + postId;
        create(tenantId, postAuthorId, "COMMENT", title, null, link);
    }

    /**
     * 관리자가 비밀번호를 초기화했을 때 대상 사용자에게 알림을 생성한다.
     */
    @Transactional
    public void notifyPasswordReset(long userId, Long tenantId, String locale) {
        String title = i18n.getMessage("notification.passwordReset.title", locale);
        String message = i18n.getMessage("notification.passwordReset.message", locale);
        create(tenantId, userId, "PASSWORD_RESET", title, message, null);
    }

    /**
     * 사용자 본인이 비밀번호를 변경했을 때 알림을 생성한다.
     */
    @Transactional
    public void notifyPasswordChanged(long userId, Long tenantId, String locale) {
        String title = i18n.getMessage("notification.passwordChanged.title", locale);
        String message = i18n.getMessage("notification.passwordChanged.message", locale);
        create(tenantId, userId, "PASSWORD_RESET", title, message, null);
    }

    /**
     * 역할이 변경되었을 때 대상 사용자에게 알림을 생성한다.
     */
    @Transactional
    public void notifyRoleChanged(long userId, Long tenantId, String oldRole, String newRole, String locale) {
        String title = i18n.getMessage("notification.roleChanged.title", locale);
        String message = i18n.getMessage("notification.roleChanged.message", locale, oldRole, newRole);
        create(tenantId, userId, "ROLE_CHANGE", title, message, null);
    }
}
