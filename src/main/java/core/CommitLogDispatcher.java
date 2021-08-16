package core;

/**
 * @author luo
 */
public interface CommitLogDispatcher {

    /**
     * 调度
     * @param request
     */
    void dispatch(DispatchRequest request);
}
