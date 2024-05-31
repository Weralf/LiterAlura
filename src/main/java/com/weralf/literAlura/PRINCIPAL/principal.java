package com.weralf.literAlura.PRINCIPAL;

import com.weralf.literAlura.modelos.*;
import com.weralf.literAlura.repository.AutorRepository; // Asegúrate de tener este repositorio
import com.weralf.literAlura.repository.LibrosRepository;
import com.weralf.literAlura.service.ConsumoAPI;
import com.weralf.literAlura.service.ConvierteDatos;

import java.util.*;
import java.util.regex.Pattern;

public class principal {
    private Scanner teclado = new Scanner(System.in);

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ ]+");
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibrosRepository repositorio;
    private AutorRepository autorRepositorio; // Repositorio para Autor
    private List<Libro> libros;

    private final String URL_BASE = "https://gutendex.com/books/";

    public principal(LibrosRepository repository, AutorRepository autorRepository) {
        this.repositorio = repository;
        this.autorRepositorio = autorRepository;
    }

    //Esta funcion basicamente verifica si lo introducido por el usuario es valido.
    public String verificarInput(Pattern pattern){
        String input;
        while (true) {
            input = teclado.nextLine();
            if (pattern.matcher(input).matches()) {
                return input;
            } else {
                System.out.println("Error! Verifique si está introduciendo correctamente lo solicitado.");
            }
        }
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar libro 
                    2 - Mostrar libros buscados
                    3 - Busqueda por idioma
                    4 - Mostrar Autores de libros buscados
                    5 - Autores vivos en un determinado año
                    6 - Cantidad de Libros por idioma
                                  
                    0 - Salir
                    """;
            System.out.println(menu);

            String input = verificarInput(NUMERIC_PATTERN);
            opcion = Integer.parseInt(input);

            switch (opcion) {
                case 1:
                    getLibro();
                    break;
                case 2:
                    mostrarLibros();
                    break;
                case 3:
                    busquedaPorIdioma();
                    break;
                case 4:
                    mostrarAutores();
                    break;
                case 5:
                    autoresVivosEnUnAño();
                    break;
                case 6:
                    cantidadLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }
    // Se busca el titulo introducido en la API, luego se verifica si esta en la base de datos o no, lo mismo con el autor
    // Si esta en la base de datos se retorna la info desde la misma, sino, se guarda en ella y se retornan los datos.
    private Libro getLibro() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        String nombreLibro = verificarInput(ALPHANUMERIC_PATTERN);

        var json = consumoAPI.obtenerDatos(URL_BASE+"?search="+nombreLibro.replace(" ", "+"));
        var resultadoBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = resultadoBusqueda.resultado().stream()
                .filter(l -> l.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst();

        if (libroBuscado.isPresent()) {
            System.out.println("¡Se encontró el libro!");
            var libroDato = libroBuscado.get();
            Libro libro = new Libro(libroDato);

            // Verificar si el autor ya existe en la base de datos
            Autor autor = new Autor(libroDato.autor().get(0)); // Ajusta según tus necesidades
            Optional<Autor> autorExistente = autorRepositorio.findByNombre(autor.getNombre());
            if (autorExistente.isPresent()) {
                libro.setAutor(autorExistente.get());
            } else {
                autorRepositorio.save(autor);
                libro.setAutor(autor);
            }
            // Verificar si el libro ya existe en la base de datos
            Optional<Libro> libroExistente = repositorio.findByTituloContainsIgnoreCase(libro.getTitulo());
            if (libroExistente.isPresent()) {
                System.out.println(libroExistente.get());
            } else {
                repositorio.save(libro);
                System.out.println(libro);
                return libro;
            }

        } else {
            System.out.println("Libro no encontrado :(");
        }

        return null;
    }

    // El usuario elige el idioma, luego se busca en la base de datos todos los libros con ese idioma y se muestran
    private void busquedaPorIdioma(){
        String idiomaSeleccionado = null;
        System.out.println("Eliga el idioma: ");
        var menu = """
                    1 - Español
                    2 - Ingles
                    """;
        System.out.println(menu);
        String input = verificarInput(NUMERIC_PATTERN);
        var idioma = Integer.parseInt(input);
        switch (idioma){
            case 1:
                idiomaSeleccionado = "es";
                break;
            case 2:
                idiomaSeleccionado = "en";
                break;
            default:
                System.out.println("Opcion invalida");
        }
        List<Libro> librosPorIdioma = repositorio.findByIdioma(idiomaSeleccionado);
        System.out.println("Libros en el idioma seleccionado:");
        librosPorIdioma.forEach(s -> System.out.println("Titulo: "+s.getTitulo()+", Autor: "+s.getAutor().getNombre()+" Descargas: "+s.getDownload_count()));
    }

    private void  mostrarAutores(){
        var autores = autorRepositorio.findAll();

        autores.stream()
                .forEach(System.out::println);
    }

    // Se busca en la base de datos los autores nacidos antes o en el mismo año introducido y que hayan fallecido despues del mismo.
    public void autoresVivosEnUnAño(){
        System.out.println("Especifique el año: ");
        String input = verificarInput(NUMERIC_PATTERN);
        var año = Integer.parseInt(input);
        var resultado = autorRepositorio.autoresVivosEnDeterminadoAño(año);
        if(resultado.isEmpty()) {
            System.out.println("No se encontraron resultados");
        } else {
            System.out.println("Autores vivos en el año "+año+":");
            List<Autor> autoresDelAño = resultado;
            autoresDelAño.forEach(System.out::println);
        }
    }

    // El usuario elige el idioma, luego se busca en la base de datos todos los libros con ese idioma
    // Se retorna el tamaño de la lista de los libros encontrados con ese idioma en la base de datos.
    public void cantidadLibrosPorIdioma(){
        String idiomaSeleccionado = null;
        System.out.println("Eliga el idioma: ");
        var menu = """
                    1 - Español
                    2 - Ingles
                    """;
        System.out.println(menu);
        String input = verificarInput(NUMERIC_PATTERN);
        var idioma = Integer.parseInt(input);
        switch (idioma){
            case 1:
                idiomaSeleccionado = "es";
                break;
            case 2:
                idiomaSeleccionado = "en";
                break;
            default:
                System.out.println("Opcion invalida");
        }
        System.out.println("La cantidad de libros en el idioma seleccionado es: "+repositorio.findByIdioma(idiomaSeleccionado).size());
    }

    private void mostrarLibros() {
        libros = repositorio.findAll();

        libros.stream()
                .forEach(System.out::println);
    }
}
