import java.util.Comparator;
import java.util.LinkedList;

public class circularFit_gerenciador extends gerenciador_memoria{
        
    int lastBlock = 0;

    protected circularFit_gerenciador(int tamanho) {
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
        int[] lastBlockComp = {lastBlock};
        blocos_livres.sort(new Comparator<LinkedList<Integer>>() {

            @Override
            public int compare(LinkedList<Integer> o1, LinkedList<Integer> o2) {
                if(o1.contains(lastBlockComp[0])) return -1;
                else return 1;
            }
            
        });
        LinkedList<Integer> bloco = (LinkedList<Integer>) blocos_livres.get(0);
        int corteIndex = bloco.indexOf(lastBlock);
        LinkedList<Integer> sub = (LinkedList<Integer>) bloco.clone();
        if(bloco.size() > tamanho)
        {
            LinkedList<Integer> preBloco = (LinkedList<Integer>) bloco.clone();
            LinkedList<Integer> postBloco = (LinkedList<Integer>) bloco.clone();
            sub.subList(corteIndex + tamanho, bloco.size()).clear();
            sub.subList(0, corteIndex).clear();
            preBloco.subList(corteIndex, bloco.size()).clear();
            postBloco.subList(0, corteIndex + tamanho).clear();
            if(!preBloco.isEmpty()) 
                blocos_livres.add(preBloco);
            if(!postBloco.isEmpty())
                blocos_livres.add(postBloco);
            blocos_livres.remove(bloco);
            processDict.put(id, sub);
            lastBlock = bloco.get(corteIndex) + tamanho;
            return true;
        }
        for (int i = 1; i < blocos_livres.size(); i++) {
            LinkedList<Integer> linkedList = blocos_livres.get(i);
            if(linkedList.size() < tamanho) continue;
            sub = (LinkedList<Integer>) linkedList.clone();
            sub.subList(tamanho, sub.size()).clear();
            processDict.put(id, sub);
            linkedList.subList(0, tamanho).clear();
            if(linkedList.isEmpty()) blocos_livres.remove(linkedList);
            return true;

        }
        System.out.println("No space for process alocation");
        return false;
    }
}
