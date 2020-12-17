package info.tomacla.biketeam.domain.global;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalDataRepository extends CrudRepository<GlobalData, Long> {

}
