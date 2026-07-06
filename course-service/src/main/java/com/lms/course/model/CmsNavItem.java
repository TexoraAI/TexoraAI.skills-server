package com.lms.course.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a single navigation link belonging to one hub's
 * navigation list (Student Hub, Trainer Hub, Admin Hub each maintain their
 * own separate ordered list of nav items, scoped by {@code pageKey}).
 */
@Entity
@Table(name = "cms_nav_item")
public class CmsNavItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_key", nullable = false)
    private String pageKey;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String href;

    @Column(name = "open_in", nullable = false)
    private String openIn = "same_tab";

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    public CmsNavItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPageKey() {
        return pageKey;
    }

    public void setPageKey(String pageKey) {
        this.pageKey = pageKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getOpenIn() {
        return openIn;
    }

    public void setOpenIn(String openIn) {
        this.openIn = openIn;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}