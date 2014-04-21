package org.wso2.carbon.cassandra.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.common.auth.Action;
import org.wso2.carbon.cassandra.common.auth.AuthUtils;
import org.wso2.carbon.cassandra.server.internal.CassandraServerDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class TenantCreationListener implements TenantMgtListener {

    private static Log log = LogFactory.getLog(TenantCreationListener.class);
    private static final int EXEC_ORDER = 40;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            cc.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            UserRealmService realmService = CassandraServerDataHolder.getInstance().getRealmService();
            int tenantID = realmService.getTenantManager().getTenantId(tenantInfoBean.getTenantDomain());
            UserRealm userRealm = realmService.getTenantUserRealm(tenantID);
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            for (String action : Action.ALL_ACTIONS_ARRAY) {
                authorizationManager.authorizeRole(userRealm.getRealmConfiguration().getAdminRoleName(),
                        AuthUtils.RESOURCE_PATH_PREFIX, action);
            }
        } catch (UserStoreException e) {
            log.error("Setting Cassandra permissions for tenant admin role failed at onTenantCreate event.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s2) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Do nothing
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s2) throws StratosException {
        // Do nothing
    }

    @Override
    public int getListenerOrder() {
        return EXEC_ORDER;
    }
}
