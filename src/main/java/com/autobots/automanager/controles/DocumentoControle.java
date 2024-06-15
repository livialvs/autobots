package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelos.DocumentoAtualizador;
import com.autobots.automanager.modelos.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/documentos")
public class DocumentoControle {

    @Autowired
    private ClienteRepositorio repositorio;

    @Autowired
    private DocumentoRepositorio repositorioDocumento;

    @Autowired
    private DocumentoSelecionador selecionadorDocumento;

    // get all
    @GetMapping
    public ResponseEntity<List<Documento>> obterDocumentos() {
        List<Documento> documentos = repositorioDocumento.findAll();
        return documentos.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NOT_FOUND) : 
                new ResponseEntity<>(documentos, HttpStatus.OK);
    }

    // get by id
    @GetMapping("/{docId}")
    public ResponseEntity<Documento> obterDocumento(@PathVariable long docId) {
        Documento documento = selecionadorDocumento.selecionar(repositorioDocumento.findAll(), docId);
        return documento == null ? 
                new ResponseEntity<>(HttpStatus.NOT_FOUND) : 
                new ResponseEntity<>(documento, HttpStatus.OK);
    }

    // cadastro de documento no cliente
    @PostMapping("/cadastro/{clienteId}")
    public ResponseEntity<Void> cadastrarDocumento(@PathVariable long clienteId, @RequestBody Documento documento) {
        Cliente cliente = repositorio.findById(clienteId).orElse(null);
        if (cliente != null) {
            documento = repositorioDocumento.save(documento); 
            cliente.getDocumentos().add(documento); 
            repositorio.save(cliente);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // atualizar documento
    @PutMapping("/atualizar/{docId}")
    public ResponseEntity<Void> atualizarDocumento(@PathVariable long docId, @RequestBody Documento atualizacao) {
        Documento documento = repositorioDocumento.findById(docId).orElse(null);
        if (documento != null) {
            new DocumentoAtualizador().atualizar(documento, atualizacao);
            repositorioDocumento.save(documento);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // deletar documento
    @DeleteMapping("/deletar/{clienteId}/{docId}")
    public ResponseEntity<Void> deletarDocumento(@PathVariable long clienteId, @PathVariable long docId) {
        Cliente cliente = repositorio.findById(clienteId).orElse(null);
        Documento documento = repositorioDocumento.findById(docId).orElse(null);
        if (cliente != null && documento != null && cliente.getDocumentos().contains(documento)) {
            cliente.getDocumentos().remove(documento);
            repositorioDocumento.delete(documento);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
