package org.wso2.carbon.hdfs.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import java.util.ArrayList;
import java.util.List;

public class HDFSPermissionAdmin extends HDFSAdmin {

    private static Log log = LogFactory.getLog(HDFSAdminComponentManager.class);
    private static final String HDFS_ROLE_PERMISSION_PATH = "/hdfs/permission";

    //Ideally must move to another admin.
    public boolean addRole(String roleName, String user, HDFSPermissionBean permission) throws HDFSServerManagementException {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        UserStoreManager currentTenantsUserStore;
        String[] users = {user};
        roleName = carbonContext.getTenantDomain() + "_" + roleName;
        Permission[] permissions = new Permission[3];
        HDFSPermissionEntry perm = permission.getRolePermissions();
        Permission readperm = null;
        Permission writeperm = null;
        Permission executeperm = null;

        if (perm.isReadAllow()) {
            readperm = new Permission(HDFS_ROLE_PERMISSION_PATH, "GET");
        }
        if (perm.isWriteAllow()) {
            writeperm = new Permission(HDFS_ROLE_PERMISSION_PATH, "EDIT");
        }
        if (perm.isExecuteAllow()) {
            executeperm = new Permission(HDFS_ROLE_PERMISSION_PATH, "BROWSE");
        }
        permissions[0] = readperm;
        permissions[1] = writeperm;
        permissions[2] = executeperm;
        try {
            currentTenantsUserStore = carbonContext.getUserRealm().getUserStoreManager();
            if (!currentTenantsUserStore.isExistingRole(roleName)) {
                currentTenantsUserStore.addRole(roleName, users, permissions);
                return true;
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving folder information";
            handleException(msg, e);
        }

        return false;
    }

    public List<String> getTenantUsers() {
        List<String> usersList = new ArrayList<String>();
        String[] users = null;
        try {
            users = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager().listUsers("*", -1);
        } catch (UserStoreException e) {
            // TODO Auto-generated catch block
            log.error("User store exception occurred while getting tenant users.", e);
        }
        if (users != null) {
            for (String user : users) {
                usersList.add(user);
            }
        }
        return usersList;
    }

    public HDFSPermissionBean[] getHDFSRolesWithPermissions() {
        List<HDFSPermissionBean> roleList = new ArrayList<HDFSPermissionBean>();
        try {
            CarbonContext cc = CarbonContext.getThreadLocalCarbonContext();
            AuthorizationManager authorizationManager = cc.getUserRealm().getAuthorizationManager();
            String[] allRoles = cc.getUserRealm().getUserStoreManager().getRoleNames();
            for (String role : allRoles) {
                if (role.startsWith(cc.getTenantDomain())) {
                    HDFSPermissionBean permissionBean = new HDFSPermissionBean();
                    permissionBean.setRoleName(role);

                    HDFSPermissionEntry permissionEntry = new HDFSPermissionEntry();
                    try{
                        boolean isAuthorized = authorizationManager.isRoleAuthorized(role,
                                HDFS_ROLE_PERMISSION_PATH, "GET");
                        if (isAuthorized) {
                            permissionEntry.setReadAllow(true);
                        } else {
                            permissionEntry.setReadAllow(false);
                        }
                    } catch (UserStoreException ex){
                        log.error(ex.getMessage(),ex);
                    }
                    try{
                        boolean isAuthorized = authorizationManager.isRoleAuthorized(role,
                                HDFS_ROLE_PERMISSION_PATH, "EDIT");
                        if (isAuthorized) {
                            permissionEntry.setWriteAllow(true);
                        } else {
                            permissionEntry.setWriteAllow(false);
                        }
                    } catch (UserStoreException ex){
                        log.error(ex.getMessage(),ex);
                    }
                    try{
                        boolean isAuthorized = authorizationManager.isRoleAuthorized(role,
                                HDFS_ROLE_PERMISSION_PATH, "BROWSE");
                        if (isAuthorized) {
                            permissionEntry.setExecuteAllow(true);
                        } else {
                            permissionEntry.setExecuteAllow(false);
                        }
                    } catch (UserStoreException ex){
                        log.error(ex.getMessage(),ex);
                    }
                    permissionBean.setRolePermissions(permissionEntry);
                    roleList.add(permissionBean);
                }
            }
        } catch (UserStoreException e) {
            log.error("User store exception occurred while getting HDFS roles.", e);
        }
        return roleList.toArray(new HDFSPermissionBean[roleList.size()]);
    }

    public boolean updateRole(String roleName, String user, HDFSPermissionBean permission) throws HDFSServerManagementException {
        return false;
    }

    public boolean deleteRole(String roleName, String user) {
        return false;
    }

    protected void handleException(String msg, Exception e) throws HDFSServerManagementException {
        log.error(msg, e);
        throw new HDFSServerManagementException(msg, log);
    }


}
