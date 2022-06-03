package cauBlackHole.photoragemember.adapter.persistence;

/*@Repository
@Slf4j
public class MemoryUserRepository implements UserRepository {

    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;

    @Override
    public void reset() {
        store.clear();
    }

    @Override
    public Member save(Member user) {
        user.setId(++sequence);
        log.info("save: user = {}", user);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Member user) {
        log.info("delete : user={}", user);
        store.remove(user.getId());
    }

    @Override
    public Optional<Member> findById(Long id) {
        Optional<Member> user = Optional.ofNullable(store.get(id));
        if(user.isPresent()){
            log.info("findById : user = {}", user.get());
        }
        else{
            log.info("no Users");
        }
        return user;
    }

    @Override
    public List<Member> findAll() {
        log.info("findAll : users = {}", store.values());
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Member> findByName(String name) {
        List<Member> findUsers = findAll().stream().
                filter(member -> member.getName().equals(name)).
                collect(Collectors.toList());
        log.info("find by name = {}", findUsers);
        return findUsers;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Optional<Member> findUser = findAll().stream().
                filter(member -> member.getEmail().equals(email)).
                findFirst();

        if(findUser.isPresent()){
            log.info("find by email = {}", findUser.get());
        }
        else{
            log.info("no Users");
        }
        return findUser;
    }
}*/
