package model.pet.entity;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GemAdditionDto {
    private List<Integer> buffIds;
    private Map<Integer,Integer> propertyAddition;
}
