package study.spring_data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.spring_data_jpa.entity.Item;

public interface ItemRepository extends JpaRepository<Item,String> {
}
