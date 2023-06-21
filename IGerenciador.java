public interface IGerenciador {
    
    public abstract boolean alocar(String id, int tamanho);

    public boolean desalocar(String id);
}
