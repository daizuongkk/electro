package com.daizuongkk.web.repository;

import com.daizuongkk.web.model.VerificationChannel;
import com.daizuongkk.web.util.JDBCUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class VerificationOtpRepository {

    public boolean create(Long userId, VerificationChannel channel, String targetValue, String otp) {
        if (userId == null || channel == null || targetValue == null || otp == null ) {
            return false;
        }

        String expireSql = """
                UPDATE verification_otps
                SET consumed = 1
                WHERE user_id = ? AND channel = ? AND consumed = 0
                """;
        String insertSql = """
                INSERT INTO verification_otps (user_id, channel, target_value, otp_hash, expires_at)
                VALUES (?, ?, ?, ?, DATE_ADD(NOW(), INTERVAL 10 MINUTE))
                """;

        try (Connection connection = JDBCUtils.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement expireStatement = connection.prepareStatement(expireSql);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                expireStatement.setLong(1, userId);
                expireStatement.setString(2, channel.name());
                expireStatement.executeUpdate();

                insertStatement.setLong(1, userId);
                insertStatement.setString(2, channel.name());
                insertStatement.setString(3, targetValue);
                insertStatement.setString(4, BCrypt.hashpw(otp, BCrypt.gensalt(10)));

                if (insertStatement.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
            }
            connection.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public VerifyResult verify(Long userId, VerificationChannel channel, String targetValue, String otp) {
        String normalizedOtp = normalizeOtp(otp);
        if (userId == null || channel == null || targetValue == null || normalizedOtp == null) {
            return VerifyResult.INVALID;
        }

        String findSql = """
                SELECT id, otp_hash, attempts
                FROM verification_otps
                WHERE user_id = ?
                  AND channel = ?
                  AND target_value = ?
                  AND consumed = 0
                  AND expires_at >= NOW()
                ORDER BY created_at DESC
                LIMIT 1
                """;
        String attemptSql = "UPDATE verification_otps SET attempts = attempts + 1 WHERE id = ?";
        String consumeSql = "UPDATE verification_otps SET consumed = 1 WHERE id = ?";

        try (Connection connection = JDBCUtils.getConnection();
             PreparedStatement findStatement = connection.prepareStatement(findSql)) {
            findStatement.setLong(1, userId);
            findStatement.setString(2, channel.name());
            findStatement.setString(3, targetValue);

            try (ResultSet resultSet = findStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return VerifyResult.EXPIRED_OR_MISSING;
                }

                long otpId = resultSet.getLong("id");
                int attempts = resultSet.getInt("attempts");
                if (attempts >= 5) {
                    return VerifyResult.TOO_MANY_ATTEMPTS;
                }

                if (!BCrypt.checkpw(normalizedOtp, resultSet.getString("otp_hash"))) {
                    try (PreparedStatement attemptStatement = connection.prepareStatement(attemptSql)) {
                        attemptStatement.setLong(1, otpId);
                        attemptStatement.executeUpdate();
                    }
                    return VerifyResult.INVALID;
                }

                try (PreparedStatement consumeStatement = connection.prepareStatement(consumeSql)) {
                    consumeStatement.setLong(1, otpId);
                    consumeStatement.executeUpdate();
                }
                return VerifyResult.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return VerifyResult.FAILED;
        }
    }

    public enum VerifyResult {
        SUCCESS,
        INVALID,
        EXPIRED_OR_MISSING,
        TOO_MANY_ATTEMPTS,
        FAILED
    }

    private String normalizeOtp(String otp) {
        if (otp == null) {
            return null;
        }

        String normalized = otp.trim().replaceAll("\\D", "");
        return normalized.length() == 6 ? normalized : null;
    }
}
