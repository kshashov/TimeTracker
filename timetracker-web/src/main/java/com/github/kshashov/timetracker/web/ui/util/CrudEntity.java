package com.github.kshashov.timetracker.web.ui.util;

import lombok.Getter;

@Getter
public class CrudEntity<T> {

    private T entity;
    private CrudAccess access;

    public CrudEntity(T entity, CrudAccess access) {
        this.entity = entity;
        this.access = access;
    }

    public enum CrudAccess {
        DENIED(false, false, false, false, false),
        READ_ONLY(true, false, false, false, false),
        FULL_ACCESS(true, true, true, true, true);

        private boolean canView;
        private boolean canCreate;
        private boolean canEdit;
        private boolean canDelete;
        private boolean canEnable;

        CrudAccess(boolean canView, boolean canCreate, boolean canEdit, boolean canDelete, boolean canEnable) {
            this.canView = canView;
            this.canCreate = canCreate;
            this.canEdit = canEdit;
            this.canDelete = canDelete;
            this.canEnable = canEnable;
        }

        public boolean canView() {
            return canView;
        }

        public boolean canCreate() {
            return canCreate;
        }

        public boolean canEdit() {
            return canEdit;
        }

        public boolean canDelete() {
            return canDelete;
        }

        public boolean canEnable() {
            return canEnable;
        }
    }
}
