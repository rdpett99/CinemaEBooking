import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class editMovie {

    private ResultSet results;
    final String secretKey = "ylwqc";
    SendEmail email = new SendEmail();

    // This function allows the admin to add a new movie to the database
    // The input is an array of strings that contain the movie information with a certain order
    public String editMovieEx(String[] inputs, Connection connection) {
        
        //Connects to the database
        try {
            String findMovie = "select * from cinemabookingsystem.movie WHERE ? = movieID";
            PreparedStatement findMovieStmt = connection.prepareStatement(findMovie);
            findMovieStmt.setString(1, inputs[13]);
            results = findMovieStmt.executeQuery();
            if(!results.next()) {
                return "MOVIE NOT FOUND";
            }
            //Creates the SQL statement
            String sql = "UPDATE movie SET title=?, casting=?, genre=?, director=?," +
            " producer=?, duration=?, synopsis=?, display=?," +
            "trailerPicture=?, trailerVideo=?, review=?, ratingID=?" +
            " WHERE movieID = ?";
            
            PreparedStatement preparedStmt = connection.prepareStatement(sql);
        
            try {
                //Sets the values of "question marks" in the SQL statement
                preparedStmt.setString(1, inputs[1]); // title
                preparedStmt.setString(2, inputs[2]); // casting
                preparedStmt.setString(3, inputs[3]); // genre
                preparedStmt.setString(4, inputs[4]); // director
                preparedStmt.setString(5, inputs[5]); // producer
                preparedStmt.setString(6, inputs[6]); // duration
                preparedStmt.setString(7, inputs[7]); // synopsis
                preparedStmt.setString(8, inputs[8]); // display
                preparedStmt.setString(9, inputs[9]); // trailerPicture
                preparedStmt.setString(10, inputs[10]); // trailerVideo
                preparedStmt.setString(11, inputs[11]); // review
                preparedStmt.setString(12, inputs[12]); // ratingID
                preparedStmt.setString(13, inputs[13]);
                

                //Executes the SQL statement (updates the database table)
                preparedStmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return "FAILURE";
            } // try
        } catch (SQLException e) {
            e.printStackTrace();
            return "FAILURE";
        } // try
        return "SUCCESS";

    }
    
}
