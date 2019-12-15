package sandbox.kafka.apis.serde;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Account {
    @NonNull
    private String accountNumber;
    private Double balance;
}
