package org.lunker.proxy.registrar;

import gov.nist.javax.sip.header.Via;
import org.lunker.proxy.model.RemoteAddress;

/**
 * Created by dongqlee on 2018. 3. 16..
 */
public class Registration {
    private String userKey;
    private String aor;
    private String account;
    private String domain;

    private RemoteAddress remoteAddress;
    private Via[] viaList;

    private Registration() {
    }

    public Registration(String userKey, String aor, String account, String domain) {
        this.userKey = userKey;
        this.aor = aor;
        this.account = account;
        this.domain = domain;
    }

    public Registration(String userKey, String aor, String account, String domain, Via[] viaList) {
        this.userKey = userKey;
        this.aor = aor;
        this.account = account;
        this.domain = domain;
        this.viaList = viaList;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getAor() {
        return aor;
    }

    public void setAor(String aor) {
        this.aor = aor;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    public RemoteAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(RemoteAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }


    public Via[] getViaList() {
        return viaList;
    }

    public void setViaList(Via[] viaList) {
        this.viaList = viaList;
    }
}
