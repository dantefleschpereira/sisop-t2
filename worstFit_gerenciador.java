import java.util.Comparator;
import java.util.LinkedList;

public class worstFit_gerenciador extends gerenciador_memoria{

    protected worstFit_gerenciador(int tamanho) {
        super(tamanho);
    }

    public boolean alocar(String id, int tamanho)
    {
        if(processDict.containsKey(id)) {
            System.out.println("process id " + id + " already taken");
            return false;
        }
        if(blocos_livres.isEmpty()) {
            System.out.println("ESPAÇO INSUFICIENTE DE MEMÓRIA");
            return false;
        }
        blocos_livres.sort(new Comparator<LinkedList>() {
            @Override
            public int compare(LinkedList o1, LinkedList o2) {
                return (o1.size() - o2.size()) * -1;
            }           
        });
        LinkedList<Integer> l = blocos_livres.get(0);
        if(tamanho > l.size()) {
            System.out.println("No space for process alocation");
            return false;
        }
        LinkedList<Integer> sub = (LinkedList<Integer>) l.clone();
        sub.subList(tamanho, sub.size()).clear();
        processDict.put(id, sub);
        l.subList(0, tamanho).clear();
        if(l.isEmpty()) blocos_livres.remove(l);
        return true;
    }
    
}
