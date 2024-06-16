package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.adicionadores.AdicionadorLinkUsuario;
import com.autobots.automanager.entitades.CredencialUsuarioSenha;
import com.autobots.automanager.entitades.Documento;
import com.autobots.automanager.entitades.Email;
import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.entitades.Veiculo;
import com.autobots.automanager.entitades.Venda;
import com.autobots.automanager.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.servicos.EmpresaServico;
import com.autobots.automanager.servicos.UsuarioServico;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControle {

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private EmpresaServico empresaServico;

    @Autowired
    private AdicionadorLinkUsuario adicionadorLinkUsuario;

    @Autowired
    private UsuarioSelecionador usuarioSelecionador;

    @GetMapping
    public ResponseEntity<List<Usuario>> buscarUsuarios() {
        List<Usuario> usuarios = usuarioServico.buscarUsuarios();
        if (usuarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            adicionadorLinkUsuario.adicionarLink(usuarios);
            return ResponseEntity.ok(usuarios);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioServico.buscarUsuarios(), id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        } else {
            adicionadorLinkUsuario.adicionarLink(usuario);
            return ResponseEntity.ok(usuario);
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<String> deletarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioSelecionador.selecionar(usuarioServico.buscarUsuarios(), id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        } else {
            limparRelacionamentos(usuario);
            usuarioServico.deletar(id);
            return ResponseEntity.ok("Usuário deletado com sucesso");
        }
    }

    private void limparRelacionamentos(Usuario usuario) {
        usuario.getDocumentos().clear();
        usuario.getTelefones().clear();
        usuario.getEmails().clear();
        usuario.getCredenciais().clear();
        usuario.getMercadorias().clear();
        for (Veiculo veiculo : usuario.getVeiculos()) {
            veiculo.setProprietario(null);
        }
        usuario.getVeiculos().clear();
        for (Venda venda : usuario.getVendas()) {
            if (venda.getFuncionario() != null && venda.getFuncionario().getId().equals(usuario.getId())) {
                venda.setFuncionario(null);
            }
            if (venda.getCliente() != null && venda.getCliente().getId().equals(usuario.getId())) {
                venda.setCliente(null);
            }
        }
        usuario.getVendas().clear();
        
        usuario.setEndereco(null);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<String> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        Usuario usuarioExistente = usuarioSelecionador.selecionar(usuarioServico.buscarUsuarios(), id);
        if (usuarioExistente == null) {
            return ResponseEntity.notFound().build();
        } else {
            usuarioAtualizado.setId(id);
            usuarioServico.salvarUsuario(usuarioAtualizado);
            return ResponseEntity.ok("Usuário atualizado com sucesso");
        }
    }

    @PutMapping("/credencial/{idCliente}")
    public ResponseEntity<?> registroCredencial(@RequestBody CredencialUsuarioSenha registroDeCredencial, @PathVariable Long idCliente) {
        List<Usuario> usuarios = usuarioServico.buscarUsuarios();
        Usuario usuario = usuarioSelecionador.selecionar(usuarios, idCliente);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        for (CredencialUsuarioSenha credencial : usuarioServico.buscarCredenciais()) {
            if (credencial.getNomeUsuario().equals(registroDeCredencial.getNomeUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Credencial já existe");
            }
        }
        usuario.getCredenciais().add(registroDeCredencial);
        usuarioServico.salvarUsuario(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Credencial cadastrada");
    }

    @PostMapping("/cadastro/{idEmpresa}")
    public ResponseEntity<String> cadastrarUsuario(@PathVariable Long idEmpresa, @RequestBody Usuario usuario) {
        List<Email> buscarEmails = usuarioServico.buscarEmails();
        List<Documento> buscarDocumentos = usuarioServico.buscarDocumentos();
        Empresa main = empresaServico.buscarEmpresa(idEmpresa);
        if (main == null) {
            return ResponseEntity.notFound().build();
        } else {
            if (usuario.getEmails().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Por Favor, coloque seu E-mail");
            }
            if (usuario.getDocumentos().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Por favor, Coloque um Documento");
            }
            if (validarDocumentosExistentes(usuario, buscarDocumentos)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Esse documento já está cadastrado");
            }
            if (validarEmailsExistentes(usuario, buscarEmails)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado");
            }
            salvarUsuario(main, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body("Cadastro Efetuado");
        }
    }

    private boolean validarDocumentosExistentes(Usuario usuario, List<Documento> documentosExistentes) {
        for (Documento doc : usuario.getDocumentos()) {
            for (Documento docExistente : documentosExistentes) {
                if (doc.getNumero().equals(docExistente.getNumero())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validarEmailsExistentes(Usuario usuario, List<Email> emailsExistentes) {
        for (Email email : usuario.getEmails()) {
            for (Email emailExistente : emailsExistentes) {
                if (email.getEndereco().equals(emailExistente.getEndereco())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void salvarUsuario(Empresa empresa, Usuario usuario) {
        usuario.getDocumentos().forEach(doc -> doc.setDataEmissao(new Date()));
        usuario.getTelefones().forEach(tel -> usuario.getTelefones().add(tel));
        if (usuario.getPerfis().toString().contains("FORNECEDOR")) {
            if (usuario.getMercadorias().isEmpty()) {
                ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Fornecedor encontrado sem mercadoria, por favor insira um");
            } else {
                usuarioServico.salvarUsuario(usuario);
                empresa.getUsuarios().add(usuario);
                empresa.getMercadorias().addAll(usuario.getMercadorias());
                empresaServico.salvarEmpresa(empresa);
                ResponseEntity.status(HttpStatus.CREATED).body("Cadastro Efetuado");
            }
        } else {
            usuarioServico.salvarUsuario(usuario);
            empresa.getUsuarios().add(usuario);
            empresaServico.salvarEmpresa(empresa);
            ResponseEntity.status(HttpStatus.CREATED).body("Cadastro Efetuado");
        }
    }

}
