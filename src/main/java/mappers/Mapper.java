package mappers;

public interface Mapper<TStorage, TApi> {
    TApi mapStorageToApi(TStorage storageObject);
    TStorage mapApiToStorage(TApi apiObject);
}
