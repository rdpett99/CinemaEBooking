import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.io.*;
import org.json.simple.JSONObject;

public class RequestHandler {
    private ResultSet results;
    String url = "jdbc:MySQL://localhost:3306/cinemabookingsystem"; // change port if server is on different port
    String username = "root"; // set user name to local server username
    String password = "password123"; // set password to local server password
    final String secretKey = "ylwqc";
    SendEmail email = new SendEmail();

    public String handleRequest(String message) throws IOException {
        String[] inputs = message.split(",", -2);
        String command = inputs[0];
        if (command.equals("REGISTER")) {
            message = registerUser(inputs);
        } else if (command.equals("LOGIN")) {
            message = loginUser(inputs);
        } else if (command.equals("EDIT")) {
            message = editUser(inputs);
        } else if (command.equals("CONFIRM")) {
            message = confirmation(inputs);
        } else if (command.equals("REQUESTFORGOTPW")) {
            message = requestForgotPW(inputs);
        } else if (command.equals("SUBMITFORGOTPW")) {
            message = submitForgotPW(inputs);
        } else if (command.equals("LOGOUT")) {
            message = logout();
        }
        return message;
    } // handleRequest

    public String registerUser(String[] inputs) throws IOException {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String findDupUser = "select * from cinemabookingsystem.user where ? = email";
            PreparedStatement findDupUserStmt = connection.prepareStatement(findDupUser);
            encryptObject encrypter = new encryptObject();
            Random r = new Random();
            String randomNumber = String.format("%04d", 10000 + r.nextInt(9999));
            try {
                findDupUserStmt.setString(1, inputs[4]);
                results = findDupUserStmt.executeQuery();
                if (results.next()) {
                    return "User already exists";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } // try
            String sql = "insert into user (password, firstName, lastName, email, USERTYPE, billingAddress, ACTIVE, confirm, cardnum, securitynum, expmonth, expdate)" +
                    "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStmt = connection.prepareStatement(sql);
            try {
                preparedStmt.setString(1, encrypter.encrypt(inputs[1],secretKey)); //password
                preparedStmt.setString(2, inputs[2]); //first
                preparedStmt.setString(3, inputs[3]); // last
                preparedStmt.setString(4, inputs[4]); // email
                preparedStmt.setString(5, inputs[5]); // Usertype
                preparedStmt.setString(6, inputs[6]); // billingAddress
                preparedStmt.setString(7, "0"); // active
                preparedStmt.setString(8, randomNumber); // confirm
                preparedStmt.setString(9, encrypter.encrypt(inputs[7],secretKey)); // cardnum
                preparedStmt.setString(10, encrypter.encrypt(inputs[8], secretKey)); // securitynum
                preparedStmt.setString(11, inputs[9]); // expmonth
                preparedStmt.setString(12, inputs[10]); // expdate
                preparedStmt.execute();
                SendEmail sendConfirmation = new SendEmail();
                sendConfirmation.sendEmail(inputs[4], (String)randomNumber);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } // try
            //email.sendEmail(inputs[4],randomNumber);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return "FAILURE";
        } // try
        return "SUCCESS";
    }

    public String loginUser(String[] inputs) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String findUser = "select * from cinemabookingsystem.user where ? = email and ? = password";
            PreparedStatement findUserStmt = connection.prepareStatement(findUser);
            encryptObject encrypter = new encryptObject();
            try {
                findUserStmt.setString(1, inputs[1]);
                findUserStmt.setString(2, encrypter.encrypt(inputs[2],secretKey));
                results = findUserStmt.executeQuery();
                if (results.next()) {
                    String activationStatus = results.getString("ACTIVE");
                    String findActivation = "select * from cinemabookingsystem.active where ? = ActiveID";
                    PreparedStatement findActivationStmt = connection.prepareStatement(findActivation);
                    findActivationStmt.setString(1, activationStatus);
                    ResultSet tempResults = findActivationStmt.executeQuery();
                    tempResults.next();
                    String activationType = tempResults.getString("ActiveStatus");
                    if (!activationType.equals("ACTIVE")) {
                        return "NOTACTIVE";
                    }

                    String privileges = results.getString("USERTYPE");
                    String findPrivileges = "select * from cinemabookingsystem.usertype where ? = userTypeID";
                    PreparedStatement findPrivilegesStmt = connection.prepareStatement(findPrivileges);

                    findPrivilegesStmt.setString(1, privileges);
                    tempResults = findPrivilegesStmt.executeQuery();
                    tempResults.next();
                    String userType = tempResults.getString("UserTypeName");
                    String email = results.getString("email");
                    String userID = results.getString("userID");
                    String password = encrypter.decrypt(results.getString("password"),secretKey);
                    String firstName = results.getString("firstName");
                    String lastName = results.getString("lastName");
                    String billingAddress = results.getString("billingAddress");
                    String ACTIVE = results.getString("ACTIVE");
                    String cardnum = encrypter.decrypt(results.getString("cardnum"),secretKey);
                    String securitynum = encrypter.decrypt(results.getString("securitynum"),secretKey);
                    String expmonth = results.getString("expmonth");
                    String expdate = results.getString("expdate");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userID",userID);
                    jsonObject.put("password",password);
                    jsonObject.put("firstName",firstName);
                    jsonObject.put("lastName",lastName);
                    jsonObject.put("email",email);
                    jsonObject.put("userType",userType);
                    jsonObject.put("billingAddress",billingAddress);
                    jsonObject.put("ACTIVE",ACTIVE);
                    jsonObject.put("cardnum",cardnum);
                    jsonObject.put("securitynum",securitynum);
                    jsonObject.put("expmonth",expmonth);
                    jsonObject.put("expdate",expdate);
                    FileWriter file = new FileWriter("./UserView/login-user-info.json");
                    file.write(jsonObject.toJSONString());
                    file.close();
                    return ("SUCCESS," + userID + "," + password + "," + firstName + "," + lastName + "," + email +"," + userType + "," + 
                    billingAddress + "," + ACTIVE + "," + cardnum + "," + securitynum + "," + expmonth + "," + expdate);

                } else {
                    String findUserEmail = "select * from cinemabookingsystem.user where ? = email";
                    PreparedStatement findUserEmailStmt = connection.prepareStatement(findUserEmail);

                    findUserEmailStmt.setString(1, inputs[1]);
                    results = findUserEmailStmt.executeQuery();
                    if (results.next()) {
                        return "BADPASSWORD";
                    }

                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            } // try
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "BADUSER";
    }

    public String logout() {
        JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userID","");
                    jsonObject.put("password","");
                    jsonObject.put("firstName","");
                    jsonObject.put("lastName","");
                    jsonObject.put("email","");
                    jsonObject.put("userType","");
                    jsonObject.put("billingAddress", "");
                    jsonObject.put("ACTIVE", "");
                    jsonObject.put("cardnum", "");
                    jsonObject.put("securitynum", "");
                    jsonObject.put("expmonth", "");
                    jsonObject.put("expdate", "");
                    FileWriter file;
                    try {
                        file = new FileWriter("./UserView/login-user-info.json");
                        file.write(jsonObject.toJSONString());
                        file.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return "FAILURE";
                    }
                    return "SUCCESS";
                    
    }
    public String editUser(String[] inputs) { 
        encryptObject encrypter = new encryptObject();
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String findUser = "select * from cinemabookingsystem.user where ? = email";
            PreparedStatement findUserStmt = connection.prepareStatement(findUser);
            findUserStmt.setString(1, inputs[4]);
            results = findUserStmt.executeQuery();
            if(results.next()) { 
                String userEmail = results.getString("email");
                String sql = "UPDATE user SET password = ?, firstName=?, lastName=?, email=?, USERTYPE=?, billingAddress=?, cardnum=?, securitynum=?, expmonth=?,expdate=?" +
                    "WHERE email = ?";
                    PreparedStatement preparedStmt = connection.prepareStatement(sql);
            try {
                preparedStmt.setString(1, encrypter.encrypt(inputs[1],secretKey)); //password
                preparedStmt.setString(2, inputs[2]); //firstName
                preparedStmt.setString(3, inputs[3]); //lastName
                preparedStmt.setString(4, inputs[4]); //email
                preparedStmt.setString(5, inputs[5]); //usertype
                preparedStmt.setString(6, inputs[6]); //billingaddress
                preparedStmt.setString(7, encrypter.encrypt(inputs[7],secretKey)); // cardnum
                preparedStmt.setString(8, encrypter.encrypt(inputs[8],secretKey)); // securitynum
                preparedStmt.setString(9,inputs[9]); // expmonth
                preparedStmt.setString(10,inputs[10]); // expdate
                preparedStmt.setString(11, userEmail);
                preparedStmt.execute();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } // try
            }
            else 
            System.out.println("failure");
            connection.close();
    } catch (SQLException e) {
        e.printStackTrace();
        return "FAILURE";
    } 
    return "Success";
    }

    public String confirmation(String[] inputs) { 
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String findUser = "select * from cinemabookingsystem.user where ? = email";
            PreparedStatement findUserStmt = connection.prepareStatement(findUser);
            findUserStmt.setString(1, inputs[1]);
            results = findUserStmt.executeQuery();
            if(results.next()) { 
                String confirmationCode = results.getString("confirm");
                String sql = "UPDATE user SET ACTIVE = ? where ? = email";
                PreparedStatement preparedStmt = connection.prepareStatement(sql);
                if(confirmationCode.equals(inputs[2])) { 
                    try {
                        preparedStmt.setString(1, "1");
                        preparedStmt.setString(2,inputs[1]);
                        preparedStmt.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return "FAILURE";
                    } 
                    }
                    else {
                        System.out.println(" Failed to confirm");
                        return "failure";
                    }
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return "FAILURE";
            } 
            return "success";
    }

    public String requestForgotPW(String[] inputs) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
        Random r = new Random();
        String randomNumber = String.format("%04d", 10000 + r.nextInt(9999));
        String updateUser = "UPDATE user SET confirm = ? WHERE ? = email";
        PreparedStatement updateUserStmt = connection.prepareStatement(updateUser);
        updateUserStmt.setString(1, randomNumber); // confirm
        updateUserStmt.setString(2, inputs[1]); //email
        updateUserStmt.execute();
        SendEmail sendConfirmation = new SendEmail();
        sendConfirmation.sendEmail(inputs[1], (String)randomNumber);
        connection.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return "FAILURE";
        }//try
        return "success";
    }

    public String submitForgotPW(String[] inputs) {
        encryptObject encrypter = new encryptObject();
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String findUser = "select * from cinemabookingsystem.user where ? = email";
            PreparedStatement findUserStmt = connection.prepareStatement(findUser);
            findUserStmt.setString(1, inputs[1]); // email
            results = findUserStmt.executeQuery();
            if(results.next()) { 
                String confirmationCode = results.getString("confirm");
                String sql = "UPDATE user SET password = ? where ? = email";
                PreparedStatement preparedStmt = connection.prepareStatement(sql);
                if(confirmationCode.equals(inputs[3])) { 
                    try {
                        preparedStmt.setString(1, encrypter.encrypt(inputs[2],secretKey)); // password
                        preparedStmt.setString(2,inputs[1]); // email
                        preparedStmt.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return "FAILURE";
                    } 
                    }
                    else {
                        System.out.println("BADCODE");
                        return "BADCODE";
                    }
                }
        } catch (SQLException e) {
            e.printStackTrace();
            return "FAILURE";
        }
        return "success";
    }


} // RequestHandler

