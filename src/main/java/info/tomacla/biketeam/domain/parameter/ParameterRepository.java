package info.tomacla.biketeam.domain.parameter;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends CrudRepository<Parameter, String> {


}
