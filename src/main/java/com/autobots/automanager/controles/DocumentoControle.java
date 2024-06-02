package com.autobots.automanager.controles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
public class DocumentoControle {

	@Autowired
	private ClienteRepositorio repositorio;
	
	@Autowired
	private DocumentoRepositorio repositorioDocumento;
	
	@GetMapping("/documentos")
	public List<Documento> documentos(){
		return repositorioDocumento.findAll();
	}
	
	@PutMapping("/cadastrar/documento")
	public void cadastrarDocumento(@RequestBody Cliente atualizacao) {
		Cliente alvo = repositorio.getById(atualizacao.getId());
		alvo.getDocumentos().addAll(atualizacao.getDocumentos());
		repositorio.save(alvo);
	}
	
	@PutMapping("/atualizar/documento/{id}")
	public void atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao){
		Documento doc = repositorioDocumento.getById(atualizacao.getId());
		DocumentoAtualizador atualizador = new DocumentoAtualizador();
		atualizador.atualizar(doc, atualizacao);
		repositorioDocumento.save(doc);
	}
	
	@DeleteMapping("/deletar/documento/{id}")
	public void deletarDocumento(@RequestBody Documento delecao, @PathVariable long id) {
		Cliente cliente = repositorio.getById(id);
		Documento alvo = repositorioDocumento.getById(delecao.getId());
		cliente.getDocumentos().remove(alvo);
		repositorioDocumento.delete(alvo);
	}
}
