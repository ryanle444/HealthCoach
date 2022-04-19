/*
 * Created by Sean Fleming on 2021.11.02
 */
package edu.vt.managers;

import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.UserFacade;
import edu.vt.controllers.EmailController;
import edu.vt.globals.Methods;
import edu.vt.globals.Password;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

@Named("loginManager")
@SessionScoped
public class LoginManager implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    private String username;
    private String password;
    private String code;
    private String correctCode;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserFacade bean into the instance variable 'userFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserFacade userFacade;

    @Inject
    private EmailController emailController;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCorrectCode() {
        return correctCode;
    }

    public void setCorrectCode(String correctCode) {
        this.correctCode = correctCode;
    }

    public EmailController getEmailController() {
        return emailController;
    }

    public void setEmailController(EmailController emailController) {
        this.emailController = emailController;
    }

    /*
    ================
    Instance Methods
    ================
    */

    /*
    *****************************************************
    Sign in the User if the Entered Username and Password
    are Valid and Redirect to Show the Profile Page
    *****************************************************
     */
    public String loginUser() throws MessagingException {

        // Since we will redirect to show the Profile page, invoke preserveMessages()
        Methods.preserveMessages();

        String enteredUsername = username;

        // Obtain the object reference of the User object from the entered username
        User user = userFacade.findByUsername(enteredUsername);

        if (user == null) {
            Methods.showMessage("Fatal Error", "Unknown Username!",
                    "Entered username " + enteredUsername + " does not exist!");
            return "";

        } else {
            String actualUsername = user.getUsername();

            if (!actualUsername.equals(enteredUsername)) {
                Methods.showMessage("Fatal Error", "Invalid Username!",
                        "Entered Username is Unknown!");
                return "";
            }

            /*
            Call the getter method to obtain the user's coded password stored in the database.
            The coded password contains the the following parts:
                "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
             */
            String codedPassword = user.getPassword();

            // Call the getter method to get the password entered by the user
            String enteredPassword = getPassword();

            /*
            Obtain the user's password String containing the following parts from the database
                  "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
            Extract the actual password from the parts and compare it with the password String
            entered by the user by using Key Stretching to prevent brute-force attacks.
             */
            try {
                if (!Password.verifyPassword(enteredPassword, codedPassword)) {
                    Methods.showMessage("Fatal Error", "Invalid Password!",
                            "Please Enter a Valid Password!");
                    return "";
                }
            } catch (Password.CannotPerformOperationException | Password.InvalidHashException ex) {
                Methods.showMessage("Fatal Error",
                        "Password Manager was unable to perform its operation!",
                        "See: " + ex.getMessage());
                return "";
            }

            // Verification Successful: Entered password = User's actual password

            // Initialize the session map with user properties of interest in the method below


            // Redirect to show the Profile page
            if (user.getTwoFactor().equals("Y")) {
                correctCode = emailController.sendEmail(user.getEmail());
                return "/userAccount/SignInConfirm?faces-redirect=true";
            }
            else {
                initializeSessionMap(user);
                return "/userAccount/Profile?faces-redirect=true";
            }

        }
    }

    public String confirmUser() {
        if (code.equals(correctCode)) {
            String enteredUsername = username;
            User user = userFacade.findByUsername(enteredUsername);
            initializeSessionMap(user);
            return "/userAccount/Profile?faces-redirect=true";
        }

        else {
            Methods.showMessage("Fatal Error", "Invalid Confirmation Code!",
                    "Please Enter a Valid Confirmation Code!");
            return "";
        }
    }

    /*
    ******************************************************************
    Initialize the Session Map to Hold Session Attributes of Interests 
    ******************************************************************
     */
    public void initializeSessionMap(User user) {

        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        // Store the object reference of the signed-in user
        sessionMap.put("user", user);

        // Store the First Name of the signed-in user
        sessionMap.put("first_name", user.getFirstName());

        // Store the Last Name of the signed-in user
        sessionMap.put("last_name", user.getLastName());

        // Store the Username of the signed-in user
        sessionMap.put("username", username);

        // Store signed-in user's Primary Key in the database
        sessionMap.put("user_id", user.getId());
    }

}

