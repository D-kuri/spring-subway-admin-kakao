package subway.line;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;
import subway.exceptions.InvalidValueException;
import subway.section.SectionDao;
import subway.station.Station;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class LineDao {
    @Autowired
    SectionDao sectionDao;
    private JdbcTemplate jdbcTemplate;

    public LineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Line save(Line line) {
        List<Line> lines = findAll();
        if(lines.stream().anyMatch((Line lineSaved) ->
                lineSaved.getName().equals(line.getName()) &&
                lineSaved.getUpStationId(sectionDao) == line.getUpStationId(sectionDao) &&
                        lineSaved.getDownStationId(sectionDao) == line.getDownStationId(sectionDao)
        )){
            throw new InvalidValueException();
        }

        String SQL = "insert into LINE (name) values (?)";
        int stationId = jdbcTemplate.update(SQL, new Object[]{line.getName(), line.getColor(), line.getExtraFare()});

        return findById(Long.valueOf(stationId));
    }

    public List<Line> findAll() {
        return jdbcTemplate.query("select * from LINE",
                (rs, rowNum) -> {
                    Line newLine = new Line(
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getInt("extraFare")
                    );
                    return newLine;
                });
    }

    public Line findById(Long id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM LINE where id = ?",
                (rs, rowNum) -> { Line newLine = new Line(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getInt("extraFare")
                    );
                return newLine;
                }, id);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("delete from LINE where id = ?", id);
    }


}
