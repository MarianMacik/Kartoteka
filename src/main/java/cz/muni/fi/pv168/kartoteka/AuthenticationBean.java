/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.kartoteka;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Permission;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.exception.UserDeniedPermissionException;
import org.brickred.socialauth.util.SocialAuthUtil;

@Named
@SessionScoped
public class AuthenticationBean implements Serializable {

    private SocialAuthManager manager;
    private String originalURL;
    private String providerID;
    private Profile profile;

    public AuthenticationBean() {
    }

    public void socialConnect() throws Exception {
        // Put your keys and secrets from the providers here 
        Properties props = System.getProperties();
        String FACEBOOK_APP_ID = "672049939523091";
        String FACEBOOK_APP_SECRET = "031990dbcc5d28705901d5f9db0777d1";

        String GOOGLE_ID = "668977671514-museeg57hpglh6p812cneqgfl5ut033s.apps.googleusercontent.com";
        String GOOGLE_SECRET = "x6SjfTpLd7UUFivLJO9IksHx";

        if ("facebook".equals(providerID)) {
            props.put("graph.facebook.com.consumer_key", FACEBOOK_APP_ID);
            props.put("graph.facebook.com.consumer_secret", FACEBOOK_APP_SECRET);
            props.put("graph.facebook.com.custom_permissions", "publish_stream,email,user_birthday,user_location,offline_access");
        } else {
            props.put("www.google.com.consumer_key", GOOGLE_ID);
            props.put("www.google.com.consumer_secret", GOOGLE_SECRET);
        }

        // Define your custom permission if needed
        //props.put("graph.facebook.com.custom_permissions", "publish_stream,email,user_birthday,user_location,offline_access");
        //props.put("googleapis.com.custom_permissions", "https://www.googleapis.com/auth/userinfo.profile,profile,email");
        // Initiate required components
        SocialAuthConfig config = SocialAuthConfig.getDefault();
        config.load(props);
        manager = new SocialAuthManager();
        manager.setSocialAuthConfig(config);

        String authenticationURL;
        if (providerID.equals("facebook")) {
            authenticationURL = manager.getAuthenticationUrl(providerID, "http://localhost:8080/Kartoteka/index.xhtml");
        } else {
            authenticationURL = manager.getAuthenticationUrl(providerID, "http://localhost:8080/Kartoteka/index.xhtml", Permission.AUTHENTICATE_ONLY);
        }

        FacesContext.getCurrentInstance().getExternalContext().redirect(authenticationURL);
        System.out.println(authenticationURL);
    }

    public void pullUserInfo() throws IOException {
        try {
            // Pull user's data from the provider
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
            Map map = SocialAuthUtil.getRequestParametersMap(request);
            if (this.manager != null) {
                AuthProvider provider = manager.connect(map);
                this.profile = provider.getUserProfile();

                // Do what you want with the data (e.g. persist to the database, etc.)
                System.out.println("User's Social profile: " + profile);

                // Redirect the user back to where they have been before logging in
                FacesContext.getCurrentInstance().getExternalContext().redirect(originalURL);

            } else {
                FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "/home.xhtml");
            }
        } catch (UserDeniedPermissionException ex) {
            FacesContext.getCurrentInstance().getExternalContext().redirect("http://localhost:8080/FacebookAuthentication/deniedPermission.xhtml");
        } catch (Exception ex) {
            System.out.println("UserSession - Exception: " + ex.toString());
        }
    }

    public void logOut() {
        try {
            // Disconnect from the provider
            String userToken = new String();
            if (providerID.equals("facebook")) {
                userToken = manager.getCurrentAuthProvider().getAccessGrant().getKey();
            }
            //String userToken = manager.getCurrentAuthProvider().getAccessGrant().getKey();
            manager.disconnectProvider(providerID);

            // Invalidate session
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
            externalContext.invalidateSession();

            

            if (providerID.equals("facebook")) {
                String logoutUrl = "https://www.facebook.com/logout.php?next=http://localhost:8080/FacebookAuthentication/home.xhtml&access_token=" + userToken;
                FacesContext.getCurrentInstance().getExternalContext().redirect(logoutUrl);
            } else {
                FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "/home.xhtml");
            }

            // Redirect to home page
            //FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "/home.xhtml");
        } catch (IOException ex) {
            System.out.println("UserSessionBean - IOException: " + ex.toString());
        }
    }

    // Getters and Setters
    public SocialAuthManager getManager() {
        return manager;
    }

    public void setManager(SocialAuthManager manager) {
        this.manager = manager;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

}
