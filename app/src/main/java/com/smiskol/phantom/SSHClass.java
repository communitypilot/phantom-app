package com.smiskol.phantom;

import java.io.OutputStream;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SSHClass {
    String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEogIBAAKCAQEAvol16t9E6vieTSmrdylhws3JsGeeZxoeloIAKhAmuQmrAZTP\n" +
            "VXkTqVbt23gPuYdDIm0YGw+AzLVVwbeoBL2fJ3dOBO3iwPS02chQ2e0pEjlY+KFz\n" +
            "kLE9BpyZiqwEluSrJU1qlc036NlwrWftNOIpC8ZshXgTvDTnBK1taWvIBXUA06B/\n" +
            "RawO5IMrInP11REkzqHu15c0aHv3mWnBEPo7Z5hXdtQOGhAA5JNNAIY69LimiYi1\n" +
            "AD2rcbNonCF1qYGLX6qrWihdt8EretTk7unAMF2zlq95viFEkVDtCEcxCEEt89Y3\n" +
            "3dbL4M0oEksGdS4Y+AKCsSBACHPKiazuLspgiQIDAQABAoIBAQCEhXr8RxnaC92e\n" +
            "cZMOqDuUkCjthsRHlYUczYJrvxwPqsfDq8qg+jtQlmONN+5H7eolsZcIizncJ2tj\n" +
            "9ubnlTNy8anUB9ikuA5pQsfpKuhcAoL9Ot30DzIQvS6Vopr2kEjxAu1VD40JaOLT\n" +
            "2OrE02AVDodANYoUZv8e47irkAlosQqvAvw1ZwdV+Jho/lt5yXOU8FSbYCW24ga6\n" +
            "uj1q4bwf96ppMR0S+3VNkgW9ojURdSy2N9HScf3A+91AyjR65a7I5N1CXNvTKePz\n" +
            "JWnSr1JEajcJWMUrgLSVdJ2d/ohZC7N2nUkx3SaQpUHq+OUedaxQ5VbA89mQaW/4\n" +
            "UTUaBg7hAoGBAOgNRIsS6u0GDod3G14cod1uJKVbwPxT3yh9TjMtzjTg/2PTmvjP\n" +
            "8LYVtcEqES9p/rriFuTgIUyLyBIr4+mwGbE097cK7zq72Lva8fWpZ+KfAYcr3Y3l\n" +
            "uJEu0/BT+aJei6DrdrEz909SzriTzrkLzo5SjyiDId3N0RTVk5xszD2tAoGBANIz\n" +
            "Yjy8T9wNp619JHigyPlR7rzmPPIHYdFuQbizC6LziA5PWkBSGwWzLltTk4xyr/TS\n" +
            "vi68PmGwhajhn9XVP1DeYEshPJV/0BbFBlKlGcee+JyWZziHMtzjTp0C3LxwEE6C\n" +
            "xQBlHez1oD9wrR5LfYRL9pKFMC+L6IpEz9bvRpHNAoGBANmqaFsT2a2Pet1yygcT\n" +
            "UHnGMTWyxWlquu7d6xZypvRPAQCAouM1GhOSdbTFYu1YvYpLPTJfUpzcmUUCSn0P\n" +
            "pGnmx125MgGj5n7/tuq6hym6ANLsQJwzmVcF1+OcwZKeoNbHR8ScfCS6BhJ5AvXs\n" +
            "r0otAv/7US8fOjoSxK18GHDZAn9YrVTESq1mKFyU1DaOrUYb6HTPPFJ5yKN7twgC\n" +
            "44YFOLgtUUzB1eGQhgcIgDm/BqM0pbOWA9RNYisBFC5aB5yugSIej+b/Kuyern/8\n" +
            "XaqCjI5VgR4Kuv66MSr5EjwNQzmd5Y02nXIChZ0VJnPiU/af2WwsZAPwCxYPPvhv\n" +
            "tIIRAoGAPLxtzP7rcHi76uESO5e1O2/otgWo3ytjpszYv8boH3i42OpNrX0Bkbr+\n" +
            "qaU43obY4trr4A1pIIyVID32aYq9yEbFTFIhYJaFhhxEzstEL3OQMLakyRS0w9Vs\n" +
            "2trgYpUlSBLIOmPNxonJIfnozphLGOnKNe0RWgGR8BnwhRYzu+k=\n" +
            "-----END RSA PRIVATE KEY-----\n";

    Connection connection;


    public Session getSession(String eonIP) {
        String username = "root";
        try {
            connection = new Connection(eonIP, 8022);
            connection.connect();
            Boolean isAuthenticated = connection.authenticateWithPublicKey(username, privateKey.toCharArray(), "");
            if (!isAuthenticated) {
                return null;
            }
            Session session = connection.openSession();
            session.requestDumbPTY();
            session.startShell();

            OutputStream os = session.getStdin();
            os.write("cd /data/openpilot\n".getBytes());
            os.write("python\n".getBytes());
            os.write("from selfdrive.phantom_receiver import PhantomReceiver\n".getBytes());
            os.write("PR=PhantomReceiver()\n".getBytes());
            os.write("PR.open_socket()\n".getBytes());
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean sendPhantomCommand(Session session, String enabled, String desiredSpeed, String steeringAngle, String time) {
        try {
            if (enabled.equals("true") || enabled.equals("True")) {
                enabled = "True";
            } else {
                enabled = "False";
            }
            OutputStream os = session.getStdin();
            String command = "PR.broadcast_data(" + enabled + ", " + desiredSpeed + ", " + steeringAngle + ", " + time + ")\n";
            os.write(command.getBytes());
            if (enabled.equals("False")){  //close all sessions and socks
                try {
                    os.write("PR.close_socket()\n".getBytes());
                    os.write("exit()\n".getBytes());
                    os.write("exit\n".getBytes());
                    session.close();
                    connection.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
