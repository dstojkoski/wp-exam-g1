package mk.ukim.finki.wp.kol2022.g1.service.impl;

import mk.ukim.finki.wp.kol2022.g1.model.Employee;
import mk.ukim.finki.wp.kol2022.g1.model.EmployeeType;
import mk.ukim.finki.wp.kol2022.g1.model.Skill;
import mk.ukim.finki.wp.kol2022.g1.model.exceptions.InvalidEmployeeIdException;
import mk.ukim.finki.wp.kol2022.g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2022.g1.repository.SkillRepository;
import mk.ukim.finki.wp.kol2022.g1.service.EmployeeService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, SkillRepository skillRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> listAll() {
        return this.employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return this.employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
    }

    @Override
    public Employee create(String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        List<Skill> skills = skillRepository.findAllById(skillId);
        String encryptedPassword = passwordEncoder.encode(password);

        Employee employee = new Employee(name, email, encryptedPassword, type, skills, employmentDate);
        //TODO
        return this.employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, String name, String email, String password, EmployeeType type, List<Long> skillId, LocalDate employmentDate) {
        Employee employee = this.findById(id);
        List<Skill> skills = skillRepository.findAllById(skillId);
        String encryptedPassword = passwordEncoder.encode(password);

        employee.setName(name);
        employee.setEmail(email);
        employee.setPassword(encryptedPassword);
        employee.setType(type);
        employee.setSkills(skills);
        employee.setEmploymentDate(employmentDate);

        employeeRepository.save(employee);
        return employee;
    }

    @Override
    public Employee delete(Long id) {
        Employee empl = this.employeeRepository.findById(id).orElseThrow(InvalidEmployeeIdException::new);
        this.employeeRepository.delete(empl);
        return empl;
    }

    @Override
    public List<Employee> filter(Long skillId, Integer yearsOfService) {
        Skill skill = skillId != null ? skillRepository.getById(skillId) : null;
        LocalDate date = yearsOfService != null ? LocalDate.now().minusYears(yearsOfService) : null;
        //TODO
        if(skillId != null && yearsOfService!=null) {
            return this.employeeRepository.findAllBySkillsContainingAndEmploymentDateBefore(skill, date);
        }else if(skillId != null){
            return this.employeeRepository.findAllBySkillsContaining(skill);
        }else if(yearsOfService != null){
            return this.employeeRepository.findAllByEmploymentDateBefore(date);
        }else{
            return this.employeeRepository.findAll();
        }




    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = this.employeeRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(email));


        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                employee.getPassword(),
                Stream.of(new SimpleGrantedAuthority("ROLE_"+employee.getType().toString())).collect(Collectors.toList())
        );

        return userDetails;
    }
}
