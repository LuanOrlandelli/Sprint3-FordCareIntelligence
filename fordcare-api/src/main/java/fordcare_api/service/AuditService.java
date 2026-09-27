package fordcare_api.service;

import fordcare_api.entity.AuditLog;
import fordcare_api.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(
            AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
    }

    public void register(
            String action,
            String username,
            String endpoint,
            String ipAddress
    ) {

        try {

            AuditLog auditLog = new AuditLog();

            auditLog.setAction(action);
            auditLog.setUsername(
                    username != null
                            ? username
                            : "UNKNOWN"
            );

            auditLog.setEndpoint(endpoint);
            auditLog.setIpAddress(ipAddress);

            auditLogRepository.save(auditLog);

            logger.info(
                    "SECURITY_AUDIT action={} user={} endpoint={} ip={}",
                    action,
                    username,
                    endpoint,
                    ipAddress
            );

        } catch (Exception exception) {

            /*
             * Uma falha no mecanismo de auditoria não deve
             * derrubar a API ou impedir uma resposta de segurança.
             */

            logger.error(
                    "AUDIT_FAILURE action={} endpoint={} error={}",
                    action,
                    endpoint,
                    exception.getMessage()
            );
        }
    }
}