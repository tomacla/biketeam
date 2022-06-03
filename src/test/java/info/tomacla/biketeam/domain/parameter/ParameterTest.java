package info.tomacla.biketeam.domain.parameter;

import info.tomacla.biketeam.domain.AbstractDBTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class ParameterTest extends AbstractDBTest {

    @Autowired
    private ParameterRepository parameterRepository;

    @Test
    public void parameterRepositoryTU() {

        assertFalse(parameterRepository.findById("PARAM").isPresent());

        final Parameter parameter = new Parameter();
        parameter.setName("PARAM");
        parameter.setValue("test");

        parameterRepository.save(parameter);

        final Optional<Parameter> saved = parameterRepository.findById("PARAM");
        assertTrue(saved.isPresent());
        assertEquals("test", saved.get().getValue());

        parameterRepository.deleteById("PARAM");

        assertFalse(parameterRepository.findById("PARAM").isPresent());
    }

}
