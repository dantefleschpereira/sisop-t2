import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class buddy_gerenciador implements IGerenciador{

    buddy_blocks main;
    HashMap<String, buddy_blocks> processDic = new HashMap<>();

    protected buddy_gerenciador(int tamanho) {
        LinkedList<Integer> lista = new LinkedList<>();
        for (int i = 0; i < tamanho; i++) lista.add(i);
        main = new buddy_blocks().setBusy(false).setMemoryBlock(lista).setTabs("");
    }

    class process{
        int tamanhoProcesso;
        String id;
        public process(int tamanhoProcesso, String id) {
            this.tamanhoProcesso = tamanhoProcesso;
            this.id = id;
        }        
    }

    class buddy_blocks {
        buddy_blocks parent;
        buddy_blocks left;
        buddy_blocks right;
        buddy_blocks brother;
        LinkedList<Integer> memoryBlock;
        int memorySize = 0;
        int start;
        boolean busy = false;
        process processo;
        String tabs;

        public buddy_blocks setBusy(boolean busy) {
            this.busy = busy;
            return this;
        }

        public buddy_blocks setChildren(){
            LinkedList<Integer> leftMemory = (LinkedList<Integer>) memoryBlock.clone();
            LinkedList<Integer> rightMemory = (LinkedList<Integer>) memoryBlock.clone();
            leftMemory.subList(memorySize/2, memorySize).clear();
            rightMemory.subList(0, memorySize/2).clear();
            left = new buddy_blocks().setMemoryBlock(leftMemory).setTabs(tabs + "    ").setParent(this);
            right = new buddy_blocks().setMemoryBlock(rightMemory).setTabs(tabs + "    ").setParent(this).setBrother(left);
            memoryBlock = null;
            memorySize *= 0.5;
            busy = false;
            return left.setBrother(right);
        }

        public void nullChildren()
        {
            memoryBlock = (LinkedList<Integer>) left.memoryBlock.clone();
            memoryBlock.addAll((Collection<? extends Integer>) right.memoryBlock.clone());
            left = null;
            right = null;
            memorySize *= 2;
            if(this.equals(main)) return;
            if(brother.busy || brother.left != null) return;
            parent.nullChildren();
        }

        public buddy_blocks setMemoryBlock(LinkedList<Integer> memoryBlock) {
            this.memoryBlock = memoryBlock;
            memorySize = memoryBlock.size();
            start = memoryBlock.get(0);
            return this;
        }

        public buddy_blocks setTabs(String tabs) {
            this.tabs = tabs;
            return this;
        }

        public buddy_blocks setParent(buddy_blocks parent) {
            this.parent = parent;
            return this;
        }

        public buddy_blocks setBrother(buddy_blocks brother) {
            this.brother = brother;
            return this;
        }

        public boolean blockToAlocar(int tamanho, process processo) {
            if(memorySize < tamanho) return false;
            if(left != null)
            {   
                if(left.blockToAlocar(tamanho, processo)) return true;
                if(right.blockToAlocar(tamanho, processo)) return true;
                return false;
            }
            if(busy) return false;
            if(tamanho == memorySize) {
                this.processo = processo;
                busy = true;
                processDic.put(processo.id, this);
                return true;
            }
            if(memorySize == 1 ) return false;
            return setChildren().blockToAlocar(tamanho, processo);
        }

        @Override
        public String toString() {
            if(left != null) return tabs + "PARENT" + "(" + start + "-" + (start + memorySize * 2 - 1) + ")\n" + left.toString() + right.toString();
            if(busy) return tabs + "CHILD (" + memoryBlock.get(0) + "-" + memoryBlock.get(memorySize - 1) + ") | " + memorySize + " | BOUND TO (" + processo.id + " " + processo.tamanhoProcesso + ") -> " + " | FRAGMENTACAO INTERNA = " + (memorySize - processo.tamanhoProcesso) + "\n";
            return tabs + "CHILD (" + memoryBlock.get(0) + "-" + memoryBlock.get(memorySize - 1) + ") | " + memorySize + " | LIVRE\n";
        }
    }

    @Override
    public boolean alocar(String id, int tamanho) {
        if(processDic.containsKey(id)) {
            System.out.println("process id " + id + " already taken");
            return false;
        }
        int tamanhoNecessario = tamanho == Integer.highestOneBit(tamanho)? tamanho : Integer.highestOneBit(tamanho) * 2; 
        if(main.blockToAlocar(tamanhoNecessario, new process(tamanho, id))) return true;
        System.out.println("ESPAcO INSUFICIENTE DE MEMORIA");
        return false;
    }

    @Override
    public String toString() {
        return main.toString();
    }

    @Override
    public boolean desalocar(String id) {
        if(!processDic.containsKey(id)) {
            System.out.println("process id " + id + " invalid");
            return false;
        }
        buddy_blocks block = processDic.get(id);
        block.busy = false;
        block.processo = null;
        processDic.remove(id);
        if(block.brother.busy || block.brother.left != null) return true;
        block.parent.nullChildren();
        return true;
    }
    
}
