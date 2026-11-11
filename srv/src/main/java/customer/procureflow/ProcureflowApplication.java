package customer.procureflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"customer.procureflow","custom"})
public class ProcureflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcureflowApplication.class, args);
    }
}