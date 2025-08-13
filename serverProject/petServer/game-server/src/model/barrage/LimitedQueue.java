package model.barrage;

import java.util.LinkedList;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LimitedQueue<E> extends LinkedList<E> {
    private static final long serialVersionUID = 3507071956649612210L;

    private int limit;

    @Override
    public boolean add(E o){
        super.add(o);
        while (size()>limit){
            super.remove();
        }
        return true;
    }

}
