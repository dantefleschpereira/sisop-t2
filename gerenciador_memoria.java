import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class gerenciador_memoria
{
    private List<LinkedList<Integer>> blocos_livres = new ArrayList<>();
    private List<LinkedList<Integer>> blocos_ocupados = new ArrayList<>();
    private int tamanho;
    private int lastBlock = 0;
    private HashMap<String, LinkedList<Integer>> processDict = new HashMap<>();

    static Scanner in = new Scanner(System.in);
    static StringBuilder builder = new StringBuilder();
    static PrintWriter write;
    static String politicaSelecionada;

    gerenciador_memoria(int tamanho) {
        this.tamanho = tamanho;
        blocos_livres.add(new LinkedList<>());
        for (int i = 0; i < tamanho; i++) {
            blocos_livres.get(0).add(i);
        }
        System.out.println(blocos_livres.get(0).size());
    }

    public boolean alocar(String id, int tamanho)
    {
        if(processDict.containsKey(id)) {
            System.out.println("process id " + id + " already taken");
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
        return true;
    }

    public boolean alocarC(String id, int tamanho)
    {
        if(processDict.containsKey(id)) {
            System.out.println("process id " + id + " already taken");
            return false;
        }
        int[] im = {0};
        blocos_livres.sort(new Comparator<LinkedList<Integer>>() {

            @Override
            public int compare(LinkedList<Integer> o1, LinkedList<Integer> o2) {
                if(o1.contains(im[0])) return 1;
                else return -1;
            }
            
        });
        LinkedList<Integer> bloco = (LinkedList<Integer>) blocos_livres.get(0);
        int corteIndex = bloco.indexOf(lastBlock);
        if(bloco.size() > tamanho)
        {
            LinkedList<Integer> preBloco = (LinkedList<Integer>) bloco.clone();
            LinkedList<Integer> postBloco = (LinkedList<Integer>) bloco.clone();
            LinkedList<Integer> sub = (LinkedList<Integer>) bloco.clone();
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
        int i = 0;
        for (LinkedList<Integer> linkedList : blocos_livres) {
            if(i == 0 || linkedList.size() < tamanho) {i++; continue;}
            LinkedList<Integer> sub = (LinkedList<Integer>) linkedList.clone();
            sub.subList(tamanho, sub.size()).clear();
            processDict.put(id, sub);
            linkedList.subList(0, tamanho).clear();
            return true;
        }
        System.out.println("No space for process alocation");
        return false;
    }

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
            sb.append("(" + linkedList.get(0) + "-" + linkedList.getLast() + ") | " + linkedList.size() + " |\n");
        }
        sb.append("Blocos alocados:\n");
        processDict.forEach((s, linkedList) -> sb.append(s + " -> (" + linkedList.get(0) + "-" + linkedList.getLast() + ") | " + linkedList.size() + " |\n"));
        return sb.toString();
    }

    public static void main(String[] args) {
        Pattern pattIn = Pattern.compile("^IN\\((\\w+),\\s?(\\d+)\\)$");
        Pattern pattOut = Pattern.compile("^OUT\\((\\w+)\\)$");
        System.out.println("Digite a política de alocação (1 - Worst-Fit ou 2 - Circular-Fit): ");
        politicaSelecionada = in.next();
        in.close();
        gerenciador_memoria gerenciador_memoria = new gerenciador_memoria(50);
        try (Stream<String> stream = Files.lines(Paths.get("requisicoes2.txt"))) {
            write = new PrintWriter(new File("out.txt"));
            stream.
            forEach(linha -> {
                Matcher match = pattIn.matcher(linha);
                if(match.matches())
                { 
                    System.out.println(match.group(0) + " " + match.group(1) + " " + match.group(2));
                    String id = match.group(1);
                    int tamanho = Integer.valueOf(match.group(2));
                    if(politicaSelecionada.equals("1")) gerenciador_memoria.alocar(id, tamanho);
                    else gerenciador_memoria.alocarC(id, tamanho);
                    System.out.println(gerenciador_memoria.toString());
                    return;
                }
                match = pattOut.matcher(linha);
                if(match.matches())
                {
                    System.out.println(match.group(0) + " " + match.group(1));
                    String id = match.group(1);
                    gerenciador_memoria.desalocar(id);
                    System.out.println(gerenciador_memoria.toString());
                    return;
                }
                System.out.println(linha + " does not have matching structure - SKIPPING");
            });
        } catch (Exception e) {
            System.out.println("Problemas no processamento do arquivo de entrada");
            e.printStackTrace();
        }
    }

    
}