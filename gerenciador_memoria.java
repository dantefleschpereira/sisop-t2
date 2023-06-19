import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public abstract class gerenciador_memoria
{
    protected List<LinkedList<Integer>> blocos_livres = new ArrayList<>();
    protected int tamanho;
    protected HashMap<String, LinkedList<Integer>> processDict = new HashMap<>();

    static Scanner in = new Scanner(System.in);
    static StringBuilder builder = new StringBuilder();
    static PrintWriter write;
    static String politicaSelecionada;

    protected gerenciador_memoria(int tamanho) {
        this.tamanho = tamanho;
        blocos_livres.add(new LinkedList<>());
        for (int i = 0; i < tamanho; i++) {
            blocos_livres.get(0).add(i);
        }
        System.out.println(blocos_livres.get(0).size());
    }

    public abstract boolean alocar(String id, int tamanho);

    public boolean desalocar(String id)
    {
        if(!processDict.containsKey(id)) {
            System.out.println("process id " + id + " invalid");
            return false;
        }
        blocos_livres.sort(new Comparator<LinkedList<Integer>>() {
            @Override
            public int compare(LinkedList<Integer> o1, LinkedList<Integer> o2) {
                return o1.getLast() - o2.getLast();
            }
            
        });
        LinkedList<Integer> bloco = processDict.get(id);
        int blocoAtras = -1;
        int blocoEmFrente = -1;
        int i = 0;
        for (LinkedList<Integer> linkedList : blocos_livres) {
            if(linkedList.contains(bloco.getFirst() - 1)){ 
                linkedList.addAll(bloco);
                blocoAtras = i;
                if(i == blocos_livres.size() - 1);
                if(blocos_livres.get(i + 1).contains(bloco.getLast() + 1))
                {
                    blocoEmFrente = i + 1;
                    linkedList.addAll(blocos_livres.get(i + 1));
                }
                break;
            }
            if(linkedList.contains(bloco.getLast() + 1))
            {
                blocoEmFrente = i + 1;
                linkedList.addAll(0, bloco);
                break;
            }
            i++;
        }
        processDict.remove(id);
        if(blocoAtras >= 0 && blocoEmFrente >= 0) blocos_livres.remove(blocoEmFrente);
        else if(blocoAtras == -1 && blocoEmFrente == -1) blocos_livres.add(bloco);
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Ocupacao da Memoria\n");
        sb.append("Blocos Livres:\n");
        for (LinkedList<Integer> linkedList : blocos_livres) {
            if(linkedList.isEmpty()) continue;
            sb.append("(" + linkedList.get(0) + "-" + linkedList.getLast() + ") | " + linkedList.size() + " |\n");
        }
        sb.append("Blocos alocados:\n");
        processDict.forEach((s, linkedList) -> sb.append(s + " -> (" + linkedList.get(0) + "-" + linkedList.getLast() + ") | " + linkedList.size() + " |\n"));
        return sb.toString();
    }    

    public static gerenciador_memoria factory(String politica, int tamanho)
    {
        return politica.equals("1")? new worstFit_gerenciador(tamanho) : new circularFit_gerenciador(tamanho);
    }
}