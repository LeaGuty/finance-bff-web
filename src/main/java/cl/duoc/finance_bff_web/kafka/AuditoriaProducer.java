package cl.duoc.finance_bff_web.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditoriaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void registrarConsulta(String cuentaId, String canal) {
        String mensaje = "Consulta realizada a la cuenta ID: " + cuentaId + " a través del canal: " + canal;
        
        // Se envía al tópico llamado 'auditoria-topic'
        kafkaTemplate.send("auditoria-topic", mensaje);
        
        System.out.println(">> Mensaje enviado a Kafka: " + mensaje);
    }
}